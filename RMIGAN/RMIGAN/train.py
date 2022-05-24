import itertools
import os

import sys

sys.path.append('..')

import torch

from torch import nn, cat
from torch.utils.data import DataLoader
from torchvision.utils import save_image
from tqdm import tqdm

from data import ImageDataset, data_root, transforms_train, device
from utils import *
from RMIGAN.discriminator import Discriminator, DiscriminatorMI
from RMIGAN.generator import Generator
from RMIGAN.autoEncoder import Encoder, Decoder

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

# 2) 配置网络
G_A2B = Generator().to(device)
G_B2A = Generator().to(device)
EC_A = Encoder().to(device)
EC_B = Encoder().to(device)
DC_A = Decoder().to(device)
DC_B = Decoder().to(device)
D_MI = DiscriminatorMI().to(device)
D_A = Discriminator().to(device)
D_B = Discriminator().to(device)

# 继承模型
# if os.path.exists('./model/G_A2B.pth'):
#     G_A2B.load_state_dict(torch.load('./model/G_A2B.pth', map_location=device))
# if os.path.exists('./model/G_B2A.pth'):
#     G_B2A.load_state_dict(torch.load('./model/G_B2A.pth', map_location=device))
# if os.path.exists('./model/EC_A.pth'):
#     EC_A.load_state_dict(torch.load('./model/EC_A.pth', map_location=device))
# if os.path.exists('./model/EC_B.pth'):
#     EC_B.load_state_dict(torch.load('./model/EC_B.pth', map_location=device))
# if os.path.exists('./model/DC_A.pth'):
#     DC_A.load_state_dict(torch.load('./model/DC_A.pth', map_location=device))
# if os.path.exists('./model/DC_B.pth'):
#     DC_B.load_state_dict(torch.load('./model/DC_B.pth', map_location=device))
# if os.path.exists('./model/D_MI.pth'):
#     D_MI.load_state_dict(torch.load('./model/D_MI.pth', map_location=device))
# if os.path.exists('./model/D_A.pth'):
#     D_A.load_state_dict(torch.load('./model/D_A.pth', map_location=device))
# if os.path.exists('./model/D_B.pth'):
#     D_B.load_state_dict(torch.load('./model/D_B.pth', map_location=device))

# Loss
criterion_GAN = nn.MSELoss()
criterion_cycle = nn.L1Loss()
criterion_identity = nn.L1Loss()
criterion_distill = nn.L1Loss()
criterion_MI = nn.L1Loss()

# Optimizer
opt_G = torch.optim.Adam(itertools.chain(G_A2B.parameters(), G_B2A.parameters()), lr=lr, betas=(0.5, 0.9999))
opt_AE = torch.optim.Adam(itertools.chain(EC_A.parameters(), EC_B.parameters(),
                                          DC_A.parameters(), DC_B.parameters()), lr=lr, betas=(0.5, 0.9999))
opt_D_MI = torch.optim.Adam(D_MI.parameters(), lr=lr, betas=(0.5, 0.9999))
opt_D_A = torch.optim.Adam(D_A.parameters(), lr=lr, betas=(0.5, 0.9999))
opt_D_B = torch.optim.Adam(D_B.parameters(), lr=lr, betas=(0.5, 0.9999))

# lr_scheduler
lr_scheduler_G = torch.optim.lr_scheduler.LambdaLR(opt_G, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)
lr_scheduler_AE = torch.optim.lr_scheduler.LambdaLR(opt_AE, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)
lr_scheduler_D_MI = torch.optim.lr_scheduler.LambdaLR(opt_D_MI, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)
lr_scheduler_D_A = torch.optim.lr_scheduler.LambdaLR(opt_D_A, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)
lr_scheduler_D_B = torch.optim.lr_scheduler.LambdaLR(opt_D_B, lr_lambda=LambdaLR(n_epochs, epoch, decay_epoch).step)

# 获取图像数据
input_A = torch.ones([batch_size, 3, size, size], dtype=torch.float).to(device)
input_B = torch.ones([batch_size, 3, size, size], dtype=torch.float).to(device)

# 标签
label_real = torch.ones([1], dtype=torch.float, requires_grad=False).to(device)
label_fake = torch.zeros([1], dtype=torch.float, requires_grad=False).to(device)

# buffer
I_BA_buffer = ReplayBuffer()
I_AB_buffer = ReplayBuffer()
Z_A_buffer = ReplayBuffer()
Z_B_buffer = ReplayBuffer()


# 3) 开始训练
def main():
    for epoch in range(n_epochs):
        tqdm_dataloader = tqdm(dataloader)
        for i, batch in enumerate(tqdm_dataloader):
            # 载入图像数据
            I_A = input_A.copy_(batch['A']).clone().detach()
            I_B = input_B.copy_(batch['B']).clone().detach()

            # 1 训练Generator
            opt_G.zero_grad()

            # 1.1 loss_identity
            same_I_B = G_A2B(I_B)
            loss_identity_B = criterion_identity(same_I_B, I_B)

            same_I_A = G_B2A(I_A)
            loss_identity_A = criterion_identity(same_I_A, I_A)

            # 1.2 loss_GAN
            I_AB = G_A2B(I_A)
            pred_B = D_B(I_AB)
            loss_GAN_A2B = criterion_GAN(pred_B, label_real)

            I_BA = G_B2A(I_B)
            pred_A = D_A(I_BA)
            loss_GAN_B2A = criterion_GAN(pred_A, label_real)

            # 1.3 loss_cycle
            I_BAB = G_A2B(I_BA)
            loss_cycle_A2B = criterion_cycle(I_BAB, I_B) * 10.0

            I_ABA = G_B2A(I_AB)
            loss_cycle_B2A = criterion_cycle(I_ABA, I_A) * 10.0

            # loss_G (total)
            loss_G = loss_identity_B + loss_identity_A + \
                    loss_GAN_A2B + loss_GAN_B2A + \
                    loss_cycle_A2B + loss_cycle_B2A

            loss_G.backward()
            opt_G.step()

            # 2 训练X-Dual AutoEncoder
            opt_AE.zero_grad()

            # loss_distill
            Z_A = EC_A(I_A)
            I_AB = I_AB_buffer.push_and_pop(I_AB)
            I_A2 = DC_A(Z_A)
            I_AB2 = DC_B(Z_A)
            loss_distill_A = (criterion_distill(I_A, I_A2) + \
                                criterion_distill(I_AB, I_AB2)) * 10.0

            Z_B = EC_B(I_B)
            I_BA = I_BA_buffer.push_and_pop(I_BA)
            I_B2 = DC_B(Z_B)
            I_BA2 = DC_A(Z_B)
            loss_distill_B = (criterion_distill(I_B, I_B2) + \
                                criterion_distill(I_BA, I_BA2)) * 10.0

            loss_distill = loss_distill_A + loss_distill_B

            loss_distill.backward()
            opt_AE.step()

            # 3 训练D_MI（loss_MI）
            opt_D_MI.zero_grad()

            Z_A = Z_A_buffer.push_and_pop(Z_A)
            Z_B = Z_B_buffer.push_and_pop(Z_B)
            Z_AB = EC_B(I_AB)
            Z_BA = EC_A(I_BA)

            Z_A_J = cat((Z_AB, Z_A), dim=1)
            Z_A_M = cat((Z_B, Z_A), dim=1)
            pred_Z_A_J = D_MI(Z_A_J)
            pred_Z_A_M = D_MI(Z_A_M)
            loss_D_MI_A_J = criterion_MI(pred_Z_A_J, label_real)
            loss_D_MI_A_M = criterion_MI(pred_Z_A_M, label_fake)
            loss_MI_A = - np.log2(np.exp(1)) * (
                        torch.mean(pred_Z_A_J) - \
                        torch.log(torch.mean(torch.exp(1 - pred_Z_A_M))))

            Z_B_J = cat((Z_BA, Z_B), dim=1)
            Z_B_M = cat((Z_A, Z_B), dim=1)
            pred_Z_B_J = D_MI(Z_B_J)
            pred_Z_B_M = D_MI(Z_B_M)
            loss_D_MI_B_J = criterion_MI(pred_Z_B_J, label_real)
            loss_D_MI_B_M = criterion_MI(pred_Z_B_M, label_fake)
            loss_MI_B = - np.log2(np.exp(1)) * (
                        torch.mean(pred_Z_B_J) - \
                        torch.log(torch.mean(torch.exp(1 - pred_Z_B_M))))

            loss_MI = loss_D_MI_A_J + loss_D_MI_A_M + \
                    loss_MI_A + loss_D_MI_B_J + \
                    loss_D_MI_B_M + loss_MI_B

            loss_MI.backward()
            opt_D_MI.step()

            # 4 训练Discriminator
            # 4.1 D_A
            opt_D_A.zero_grad()

            pred_real = D_A(I_A)
            loss_D_real = criterion_GAN(pred_real, label_real)
            pred_fake = D_A(I_BA.detach())
            loss_D_fake = criterion_GAN(pred_fake, label_fake)

            loss_D_A = (loss_D_real + loss_D_fake) * 0.5
            loss_D_A.backward()

            opt_D_A.step()

            # 4.2 D_B
            opt_D_B.zero_grad()

            pred_real = D_B(I_B)
            loss_D_real = criterion_GAN(pred_real, label_real)
            pred_fake = D_B(I_AB.detach())
            loss_D_fake = criterion_GAN(pred_fake, label_fake)

            loss_D_B = (loss_D_real + loss_D_fake) * 0.5
            loss_D_B.backward()

            opt_D_B.step()

            # 设置tqdm进度条前后缀
            tqdm_dataloader.set_description(f'epoch {epoch}')
            tqdm_dataloader.set_postfix(
                loss_G=f'{loss_G.item():.4f}',
                loss_D_A=f'{loss_D_A.item():.4f}',
                loss_D_B=f'{loss_D_B.item():.4f}',
                loss_distill=f'{loss_distill.item():.4f}',
                loss_MI=f'{loss_MI.item():.4f}'
            )

        lr_scheduler_G.step()
        lr_scheduler_AE.step()
        lr_scheduler_D_MI.step()
        lr_scheduler_D_A.step()
        lr_scheduler_D_B.step()

        # 保存模型
        if (epoch % 50 == 0) or (epoch == n_epochs - 1):
            save_some_examples(G_B2A, epoch)
            torch.save(G_A2B.state_dict(), './model/G_A2B.pth')
            torch.save(G_B2A.state_dict(), './model/G_B2A.pth')
            torch.save(EC_A.state_dict(), './model/EC_A.pth')
            torch.save(EC_B.state_dict(), './model/EC_B.pth')
            torch.save(DC_A.state_dict(), './model/DC_A.pth')
            torch.save(DC_B.state_dict(), './model/DC_B.pth')
            torch.save(D_MI.state_dict(), './model/D_MI.pth')
            torch.save(D_A.state_dict(), './model/D_A.pth')
            torch.save(D_B.state_dict(), './model/D_B.pth')


def save_some_examples(gen, epoch):
    a, b = data_A, data_B
    gen.eval()
    with torch.no_grad():
        a_fake = gen(b)
        a_fake = a_fake * 0.5 + 0.5  # remove normalization#
        save_image(a_fake, f"output/{epoch}_B2A.png")
    gen.train()


if __name__ == '__main__':
    main()
