from torch import nn, ones, float


class Encoder(nn.Module):
    """
    提取图像内容特征
    """
    def __init__(self):
        super(Encoder, self).__init__()
        # 下采样编码
        self.model = nn.Sequential(
            nn.ReflectionPad2d(3),
            nn.Conv2d(3, 64, kernel_size=7),
            nn.InstanceNorm2d(64),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(64, 128, kernel_size=3, stride=2, padding=1),
            nn.InstanceNorm2d(128),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.Conv2d(128, 256, kernel_size=3, stride=2, padding=1),
            nn.InstanceNorm2d(256),
            nn.LeakyReLU(negative_slope=0.2, inplace=True)
        )

    def forward(self, x):
        return self.model(x)


class Decoder(nn.Module):
    """
    根据图像内容特征还原图像
    """
    def __init__(self):
        super(Decoder, self).__init__()
        # 上采样解码
        self.model = nn.Sequential(
            nn.ConvTranspose2d(256, 128, kernel_size=3, 
                                stride=2, padding=1, 
                                output_padding=1),
            nn.InstanceNorm2d(128),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.ConvTranspose2d(128, 64, kernel_size=3, 
                                stride=2, padding=1, 
                                output_padding=1),
            nn.InstanceNorm2d(64),
            nn.LeakyReLU(negative_slope=0.2, inplace=True),

            nn.ReflectionPad2d(3),
            nn.Conv2d(64, 3, kernel_size=7),
            nn.Tanh()
        )

    def forward(self, x):
        return self.model(x)


def main():
    a = ones([1, 3, 256, 256], dtype=float)
    encoder = Encoder()
    decoder = Decoder()
    a = encoder(a)
    
    print(a.size())


if __name__ == '__main__':
    main()
