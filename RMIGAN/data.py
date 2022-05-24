import glob
import os
import random

from torch import device, cuda
from torchvision import transforms
from torch.utils.data import Dataset
from PIL import Image

data_root = '../images'
device = device('cuda' if cuda.is_available() else 'cpu')

# transform
transforms_train = [
    transforms.Resize(int(256 * 1.12), Image.BICUBIC),  # 尺寸放大
    transforms.RandomCrop(256),  # 随机剪裁到256
    transforms.RandomHorizontalFlip(),
    transforms.ToTensor(),
    transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
]

transforms_test = [
    transforms.Resize([256, 256]),
    transforms.ToTensor(),
    transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
]


class ImageDataset(Dataset):
    """
    读取数据集
    """

    def __init__(self, root='', transform=None, model='train'):
        super(ImageDataset, self).__init__()
        self.root = root
        self.transform = transforms.Compose(transform)
        # self.pathA = os.path.join(root, model, 'A/*')
        self.pathB = os.path.join(root, model, 'B/*')
        # self.listA = glob.glob(self.pathA)
        self.listB = glob.glob(self.pathB)

    def __getitem__(self, index):
        # im_pathA = self.listA[index % len(self.listA)]
        im_pathB = self.listB[index % len(self.listB)]

        # im_A = Image.open(im_pathA)
        im_B = Image.open(im_pathB)

        # im_A = self.transform(im_A)
        im_B = self.transform(im_B)
        # return {'A': im_A, 'B': im_B}
        return {'B': im_B}

    def __len__(self):
        # return max(len(self.listA), len(self.listB))
        return len(self.listB)
