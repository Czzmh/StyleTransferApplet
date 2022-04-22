from torch import nn


class ResNetBlock(nn.Module):
    """
    先pad为了保证conv前后大小不变
    步骤 pad->conv->norm->relu -> pad
    """

    def __init__(self, in_channel):
        super(ResNetBlock, self).__init__()
        conv_block = [
            nn.ReflectionPad2d(1),
            nn.Conv2d(in_channel, in_channel, kernel_size=3),
            nn.InstanceNorm2d(in_channel),
            nn.ReLU(inplace=True),

            nn.ReflectionPad2d(1),
            nn.Conv2d(in_channel, in_channel, kernel_size=3),
            nn.InstanceNorm2d(in_channel),
        ]

        self.conv_block = nn.Sequential(*conv_block)

    def forward(self, x):
        return self.conv_block(x) + x
