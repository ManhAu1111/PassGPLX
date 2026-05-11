import os
import json
import re

# Đường dẫn chuẩn trong mã nguồn của bạn
json_path = "/home/aumanh/AndroidStudioProjects/PassGPLX/composeApp/src/commonMain/composeResources/files/data.json"
img_dir = "/home/aumanh/AndroidStudioProjects/PassGPLX/composeApp/src/commonMain/composeResources/drawable"

def clean_filename(old_name):
    # Tách tên file và đuôi (.png)
    name, ext = os.path.splitext(old_name)
    # 1. Chuyển thành chữ thường
    new_name = name.lower()
    # 2. Thay thế dấu chấm, phẩy, gạch ngang, khoảng trắng thành dấu gạch dưới
    new_name = re.sub(r'[\.\,\-\s]', '_', new_name)
    # 3. Xóa sạch các ký tự lạ (chỉ giữ a-z, 0-9 và _)
    new_name = re.sub(r'[^a-z0-9_]', '', new_name)
    return new_name + ext.lower()

# ==========================================
# NHIỆM VỤ 1: CẬP NHẬT FILE JSON
# ==========================================
try:
    with open(json_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for item in data:
        if "image" in item:
            # Lấy tên file gốc (VD: "/image/I.424b.png" -> "I.424b.png")
            old_img_path = item["image"]
            old_filename = old_img_path.split('/')[-1]
            
            # Tạo tên file mới chuẩn (VD: "i_424b.png")
            new_filename = clean_filename(old_filename)
            
            # Lưu thẳng tên file mới vào JSON, vứt luôn chữ "/image/" đi
            item["image"] = new_filename

    # Ghi lại file JSON với định dạng đẹp, giữ nguyên tiếng Việt
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)
    print("✅ Đã cập nhật xong file data.json!")

except Exception as e:
    print(f"❌ Lỗi khi xử lý file JSON: {e}")

# ==========================================
# NHIỆM VỤ 2: ĐỔI TÊN FILE ẢNH THỰC TẾ
# ==========================================
try:
    count = 0
    # Lấy danh sách file trong thư mục drawable
    if os.path.exists(img_dir):
        for filename in os.listdir(img_dir):
            if filename.endswith(".png") or filename.endswith(".jpg"):
                new_filename = clean_filename(filename)
                
                # Nếu tên bị sai quy tắc thì tiến hành đổi tên
                if filename != new_filename:
                    old_file = os.path.join(img_dir, filename)
                    new_file = os.path.join(img_dir, new_filename)
                    os.rename(old_file, new_file)
                    count += 1
        print(f"✅ Đã đổi tên chuẩn xác cho {count} file ảnh trong thư mục drawable!")
    else:
        print(f"❌ Không tìm thấy thư mục ảnh: {img_dir}")
except Exception as e:
    print(f"❌ Lỗi khi đổi tên ảnh: {e}")
