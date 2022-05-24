from torch import nn, ones, float
from torch.nn import functional


class Discriminator(nn.Module):
    def __init__(self):
        super(Discriminator, self).__init__()
        self.model = nn.Sequential(
            nn.Conv2d(3, 64, kernel_size=4, stride=2, padding=1),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(64, 128, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(128),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(128, 256, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(256),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(256, 512, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(512),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(512, 1, kernel_size=4, padding=1),
            
            nn.AdaptiveAvgPool2d(1)
        )

    def forward(self, x):
        return self.model(x).squeeze().unsqueeze(0)
        # 后两维pooling  output_shape: [batch_size, 1]
        # return functional.avg_pool2d(x, x.size()[2:]).view(x.size()[0], -1).squeeze(-1)


class DiscriminatorMI(nn.Module):
    def __init__(self):
        super(DiscriminatorMI, self).__init__()
        self.model = nn.Sequential(
            nn.Conv2d(512, 256, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(256),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(256, 128, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(128),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(128, 64, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(64),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(64, 1, kernel_size=4, stride=2, padding=1),
            nn.InstanceNorm2d(1),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.AdaptiveAvgPool2d(1)
        )

    def forward(self, x):
        return self.model(x).squeeze().unsqueeze(0)


def main():
    model = Discriminator()
    modelMI = DiscriminatorMI()
    label_real = ones([1], dtype=float, requires_grad=False)

    a = ones([1, 3, 256, 256], dtype=float)
    print(model(a))

    b = ones([1, 256, 64, 64], dtype=float)
    print(modelMI(b).size())


if __name__ == '__main__':
    main()
