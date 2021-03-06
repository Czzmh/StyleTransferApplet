import torch
import os

import sys
sys.path.append('..')

from torchvision import transforms
from torchvision.utils import save_image
from torch.utils.data import DataLoader
from tqdm import tqdm

from data import ImageDataset, transforms_test, device, data_root
from CycleGAN.generator import Generator

# 1) 获取数据
size = 256

input_A = torch.ones([1, 3, size, size], dtype=torch.float).to(device)
input_B = torch.ones([1, 3, size, size], dtype=torch.float).to(device)
dataloader = DataLoader(ImageDataset(root=data_root, transform=transforms_test, model='test'),
                        batch_size=1, shuffle=False, num_workers=8)
tqdm_dataloader = tqdm(dataloader)

# 2) 定义网络
G_A2B = Generator().to(device)
G_B2A = Generator().to(device)

G_A2B.load_state_dict(torch.load('./model/G_A2B.pth', map_location=device))
G_B2A.load_state_dict(torch.load('./model/G_B2A.pth', map_location=device))

G_A2B.eval()
G_B2A.eval()

if not os.path.exists('./output'):
    os.mkdir('./output')
if not os.path.exists('./output/A2B'):
    os.mkdir('./output/A2B')
if not os.path.exists('./output/B2A'):
    os.mkdir('./output/B2A')


def main():
    for i, batch in enumerate(tqdm_dataloader):
        real_A = input_A.copy_(batch['A']).clone().detach()
        real_B = input_B.copy_(batch['B']).clone().detach()

        fake_B = 0.5 * (G_A2B(real_A).data + 1.0)
        fake_A = 0.5 * (G_B2A(real_B).data + 1.0)
        real_A = 0.5 * (real_A.data + 1.0)
        real_B = 0.5 * (real_B.data + 1.0)

        save_image(real_A, f'./output/A2B/{i}_A.png')
        save_image(fake_B, f'./output/A2B/{i}_B.png')
        save_image(real_B, f'./output/B2A/{i}_B.png')
        save_image(fake_A, f'./output/B2A/{i}_A.png')


# 3) 开始测试
if __name__ == '__main__':
    main()
