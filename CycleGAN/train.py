import itertools
import os

import sys
sys.path.append('..')

from torch import nn
from torch.utils.data import DataLoader
from torchvision.utils import save_image
from tqdm import tqdm

from data import ImageDataset, transforms_train, device, data_root
from utils import *
from CycleGAN.discriminator import Discriminator
from CycleGAN.generator import Generator

# 1) 获取数据
batch_size = 1
size = 256
lr = 0.0002
n_epochs = 200
epoch = 0
decay_epoch = 100

dataloader = DataLoader(ImageDataset(root=data_root, transform=transforms_train, model='train'),
                        batch_size=batch_size, shuffle=True, num_workers=10)

data = next(iter(dataloader))
data_A, data_B = data['A'].to(device), data['B'].to(device)
save_image(data_B * 0.5 + 0.5, f"output/#B.png")

# 2) 定义网络
G_A2B = Generator().to(device)
G_B2A = Generator().to(device)
D_A = Discriminator().to(device)
D_B = Discriminator().to(device)

# if os.path.exists('model/G_A2B.pth'):
#     G_A2B.load_state_dict(torch.load('./model/G_A2B.pth', map_location=device))
# if os.path.exists('model/G_B2A.pth'):
#     G_B2A.load_state_dict(torch.load('./model/G_B2A.pth', map_location=device))
# if os.path.exists('model/D_A.pth'):
#     D_A.load_state_dict(torch.load('./model/D_A.pth', map_location=device))
# if os.path.exists('model/D_B.pth'):
#     D_B.load_state_dict(torch.load('./model/D_B.pth', map_location=device))

# Loss
criterion_GAN = nn.MSELoss()
criterion_cycle = nn.L1Loss()
criterion_identity = nn.L1Loss()  # 真实数据和生成数据的相似度loss

# Optimizer
opt_G = torch.optim.Adam(itertools.chain(G_A2B.parameters(), G_B2A.parameters()), lr=lr, betas=(0.5, 0.9999))
optD_A = torch.optim.Adam(D_A.parameters(), lr=lr, betas=(0.5, 0.9999))
optD_B = torch.optim.Adam(D_B.parameters(), lr=lr, betas=(0.5, 0.9999))

lr_scheduler_G = torch.optim.lr_scheduler.LambdaLR(opt_G, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)
lr_scheduler_D_A = torch.optim.lr_scheduler.LambdaLR(optD_A, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)
lr_scheduler_D_B = torch.optim.lr_scheduler.LambdaLR(optD_B, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)

input_A = torch.ones([1, 3, size, size], dtype=torch.float).to(device)
input_B = torch.ones([1, 3, size, size], dtype=torch.float).to(device)
label_real = torch.ones([1], dtype=torch.float, requires_grad=False).to(device)
label_fake = torch.zeros([1], dtype=torch.float, requires_grad=False).to(device)

fake_A_buffer = ReplayBuffer()
fake_B_buffer = ReplayBuffer()


def main():
    for epoch in range(n_epochs):
        tqdm_dataloader = tqdm(dataloader)
        for i, batch in enumerate(tqdm_dataloader):
            # 载入图像数据
            real_A = input_A.copy_(batch['A']).clone().detach()
            real_B = input_B.copy_(batch['B']).clone().detach()

            # 训练Generator
            # loss_identity
            opt_G.zero_grad()
            same_B = G_A2B(real_B)
            loss_identity_B = criterion_identity(same_B, real_B) * 5.0

            same_A = G_B2A(real_A)
            loss_identity_A = criterion_identity(same_A, real_A) * 5.0

            # loss_GAN
            fake_B = G_A2B(real_A)
            pred_B = D_B(fake_B)
            loss_GAN_A2B = criterion_GAN(pred_B, label_real)

            fake_A = G_B2A(real_B)
            pred_A = D_A(fake_A)
            loss_GAN_B2A = criterion_GAN(pred_A, label_real)

            # loss_cycle
            recover_B = G_A2B(fake_A)
            loss_cycle_A2B = criterion_cycle(recover_B, real_B)

            recover_A = G_B2A(fake_B)
            loss_cycle_B2A = criterion_cycle(recover_A, real_A)

            loss_G = loss_identity_B + loss_identity_A + loss_GAN_A2B + loss_GAN_B2A + loss_cycle_A2B + loss_cycle_B2A

            loss_G.backward()
            opt_G.step()

            # 训练Discriminator
            # A
            optD_A.zero_grad()

            pred_real = D_A(real_A)
            loss_D_real = criterion_GAN(pred_real, label_real)
            fake_A = fake_A_buffer.push_and_pop(fake_A)
            pred_fake = D_A(fake_A.detach())  # 优化 loss_D_fake时不优化fake_A的参数 也就是不优化生成器
            loss_D_fake = criterion_GAN(pred_fake, label_fake)

            loss_D_A = (loss_D_real + loss_D_fake) * 0.5

            loss_D_A.backward()
            optD_A.step()

            # B
            optD_B.zero_grad()

            pred_real = D_B(real_B)
            loss_D_real = criterion_GAN(pred_real, label_real)
            fake_B = fake_B_buffer.push_and_pop(fake_B)
            pred_fake = D_B(fake_B.detach())
            loss_D_fake = criterion_GAN(pred_fake, label_fake)

            loss_D_B = (loss_D_real + loss_D_fake) * 0.5

            loss_D_B.backward()
            optD_B.step()

            # 设置tqdm进度条前后缀
            tqdm_dataloader.set_description(f'epoch {epoch}')
            tqdm_dataloader.set_postfix(loss=f'{(loss_G + loss_D_A + loss_D_B).item():.4f}')

        lr_scheduler_D_A.step()
        lr_scheduler_D_B.step()
        lr_scheduler_G.step()

        # 保存模型
        save_some_examples(G_B2A, epoch)
        torch.save(G_A2B.state_dict(), './model/G_A2B.pth')
        torch.save(G_B2A.state_dict(), './model/G_B2A.pth')
        torch.save(D_A.state_dict(), './model/D_A.pth')
        torch.save(D_B.state_dict(), './model/D_B.pth')


def save_some_examples(gen, epoch):
    a, b = data_A, data_B
    gen.eval()
    with torch.no_grad():
        a_fake = gen(b)
        a_fake = a_fake * 0.5 + 0.5  # remove normalization#
        b = b * 0.5 + 0.5
        save_image(a_fake, f"output/{epoch}_B2A.png")
    gen.train()


if __name__ == '__main__':
    main()
