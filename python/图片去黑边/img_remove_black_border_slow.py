import cv2
import os
import datetime

# 另一个版本，裁剪的有点慢
# 将当前文件夹的图片去除黑边，输出到out文件夹
threshold = 10  # 灵敏度，数值越大，裁剪掉的越多


def change_size(file):
    image = cv2.imread(file, 1)  # 读取图片 image_name应该是变量
    img = cv2.medianBlur(image, 5)  # 中值滤波，去除黑色边际中可能含有的噪声干扰
    b = cv2.threshold(img, threshold, 255, cv2.THRESH_BINARY)  # 调整裁剪效果
    binary_image = b[1]  # 二值图--具有三通道
    binary_image = cv2.cvtColor(binary_image, cv2.COLOR_BGR2GRAY)
    print(binary_image.shape)  # 改为单通道

    x = binary_image.shape[0]
    print("高度x=", x)
    y = binary_image.shape[1]
    print("宽度y=", y)
    edges_x = []
    edges_y = []
    for i in range(x):
        for j in range(y):
            if binary_image[i][j] == 255:
                edges_x.append(i)
                edges_y.append(j)

    left = min(edges_x)  # 左边界
    right = max(edges_x)  # 右边界
    width = right - left  # 宽度
    bottom = min(edges_y)  # 底部
    top = max(edges_y)  # 顶部
    height = top - bottom  # 高度

    pre1_picture = image[left:left + width, bottom:bottom + height]  # 图片截取
    return pre1_picture  # 返回图片数据


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
        cv2.imwrite(save_path + file_name, x)
        print("裁剪：", file_name, "完成")
        success += 1
    except Exception as e:
        print(file_name, "裁剪出错，可以调高下灵敏度试试")
        print(e)
print("裁剪完毕，裁剪条数：" + str(success))
endtime = datetime.datetime.now()
endtime = (endtime - starttime).seconds
print("裁剪总用时：", endtime, "秒钟")
