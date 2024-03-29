import cv2
import os
import datetime
import numpy as np

# 将当前文件夹的图片去除黑边，输出到out文件夹
threshold = 10 #灵敏度，数值越大，裁剪掉的越多


def change_size(file):

    # 中文乱码
    #src_img = cv2.imread(file)  # 导入图片
    src_img = cv2.imdecode(np.fromfile(file, dtype=np.uint8),-1)
    gray = cv2.cvtColor(src_img, cv2.COLOR_RGB2GRAY)  # 转换为灰度图像

    nrow = gray.shape[0]  # 获取图片尺寸
    ncol = gray.shape[1]

    rowc = gray[:, int(1 / 2 * nrow)]  # 无法区分黑色区域超过一半的情况
    colc = gray[int(1 / 2 * ncol), :]

    rowflag = np.argwhere(rowc > threshold)
    colflag = np.argwhere(colc > threshold)

    if len(rowflag) == 0:
        left = 0
        right = nrow
    else:
        left, right = rowflag[0, 0], rowflag[-1, 0]

    # 如果只裁剪掉一两个像素，说明是误剪
    if nrow - (right - left) <= 2:
        left = 0
        right = nrow

    if len(colflag) == 0:
        top = 0
        bottom = ncol
    else:
        top, bottom = colflag[0, 0], colflag[-1, 0]

    # 如果只裁剪掉一两个像素，说明是误剪
    if ncol - (bottom - top) <= 2:
        top = 0
        bottom = ncol

    # left, bottom, right, top = rowflag[0, 0], colflag[-1, 0], rowflag[-1, 0], colflag[0, 0]
    # cv2.imshow('name',src_img[left:right,top:bottom]) # 效果展示
    return src_img[left:right, top:bottom]


source_path = "./"  # 图片来源路径
save_path = "./out/"  # 图片修改后的保存路径

input("即将从当前目录寻找图片并去除黑边，输出到out文件夹。\n按任意键确认并继续：")

if not os.path.exists(save_path):
    os.mkdir(save_path)
file_names = os.listdir(source_path)
starttime = datetime.datetime.now()
success = 0
for i in range(len(file_names)):
    file_name = file_names[i]
    type = file_name.split(".")[-1]
    if type is None or type not in ["jpg", "jpeg", "png"]:
        continue
    if os.path.exists(save_path + file_name):
        print(file_name, "已存在，跳过")
        continue
    try:
        x = change_size(source_path + file_name)
        # 中文乱码
        #cv2.imwrite(save_path + file_name, x)
        cv2.imencode('.jpg', x)[1].tofile(save_path + file_name)
        print("裁剪：", file_name, "完成")
        success += 1
    except Exception as e:
        print(file_name, "裁剪出错，可以调高下灵敏度试试")
        print(e)
print("裁剪完毕，裁剪条数：" + str(success))
endtime = datetime.datetime.now()
endtime = (endtime - starttime).seconds
print("裁剪总用时：", endtime, "秒钟")
