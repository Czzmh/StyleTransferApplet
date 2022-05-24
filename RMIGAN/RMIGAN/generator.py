import sys
sys.path.append('..')

from torch import nn, cat, ones, float

from RMIGAN.resNet import ResNetBlock


class Generator(nn.Module):
    """
    输入形状 (batch_size, 3, 256,256)
    输出一张图片
    """

    def __init__(self):
        super(Generator, self).__init__()
        # 下采样
        model = [nn.ReflectionPad2d(3),
                 nn.Conv2d(3, 64, kernel_size=7),
                 nn.InstanceNorm2d(64),
                 nn.ReLU(inplace=True),

                 nn.Conv2d(64, 128, kernel_size=3, stride=2, padding=1),
                 nn.InstanceNorm2d(128),
                 nn.ReLU(inplace=True),

                 nn.Conv2d(128, 256, kernel_size=3, stride=2, padding=1),
                 nn.InstanceNorm2d(256),
                 nn.ReLU(inplace=True)
                 ]

        # resBlock(9层):
        for _ in range(9):
            model += [ResNetBlock(256)]

        # 上采样
        model += [
            nn.ConvTranspose2d(256, 128, kernel_size=3, 
                                stride=2, padding=1, 
                                output_padding=1),
            nn.InstanceNorm2d(128),
            nn.ReLU(inplace=True),

            nn.ConvTranspose2d(128, 64, kernel_size=3, 
                                stride=2, padding=1, 
                                output_padding=1),
            nn.InstanceNorm2d(64),
            nn.ReLU(inplace=True),

            nn.ReflectionPad2d(3),
            nn.Conv2d(64, 3, kernel_size=7),
            nn.Tanh()
        ]

        self.model = nn.Sequential(*model)

    def forward(self, x):
        return self.model(x)


def main():
    model = Generator()
    a = ones([1, 3, 256, 256], dtype=float)
    print(model(a).size())


if __name__ == '__main__':
    main()
