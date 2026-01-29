# Definition of Done (DoD) - NextStep Project

> Áp dụng cho mọi User Story/Task trong tất cả Sprint

---

## 🧩 Code

- [ ] Code compile/build **thành công**, không lỗi hệ thống.
- [ ] Tuân thủ coding convention:
  - Java: `camelCase` cho biến, `PascalCase` cho class, tách rõ `Controller / Service / Repository`.
  - Vue: đặt tên component đúng chuẩn, **không để logic phức tạp trong template**.
- [ ] **Không còn code thừa**, comment rác, `console.log`, `System.out.println`.
- [ ] **Đã format code bằng Spotless** (auto-format, không cần note các rule SonarLint).
- [ ] Có **unit test cho các API/logic quan trọng**.
- [ ] API đã được test bằng **Postman hoặc công cụ tương đương**.
- [ ] Code được **push lên branch riêng theo từng module**, sau đó merge vào `main`.

---

## 🗄️ Database

- [ ] Mọi thay đổi DB được quản lý bằng **Flyway**.
- [ ] File migration lưu đúng chuẩn:
src/main/resources/db/migration/V1__init_schema.sql
- [ ] Migration chạy **thành công ở local**.
- [ ] Cấu trúc DB phản ánh đúng yêu cầu task.
- [ ] Không gây lỗi truy vấn hoặc ảnh hưởng dữ liệu hiện có.

---

## 🧪 Test

- [ ] Manual test đầy đủ **luồng chính & edge case**.
- [ ] Unit test / Integration test **pass toàn bộ**.
- [ ] Không còn bug/blocker liên quan đến task.
- [ ] Frontend (nếu có): test trên trình duyệt chính (Chrome, Firefox).

---

## 🚀 CI/CD & Deployment

- [ ] Build chạy OK ở local (Spring Boot + Vue).
- [ ] Sau khi merge vào `main`, **CI/CD action chạy thành công**:
- [ ] Spotless check
- [ ] OWASP dependency check
- [ ] Ứng dụng được **build thành Docker image**.
- [ ] App chạy thử sau build, **UI & API hoạt động đúng**.
- [ ] Không có lỗi console hoặc warning nghiêm trọng.
- [ ] Commit message rõ ràng, đúng nội dung thay đổi.

---

## 🔐 Security & Performance

- [ ] Không hard-code dữ liệu nhạy cảm (password, token, secret).
- [ ] API không có lỗi bảo mật cơ bản (SQL Injection, CORS, auth).
- [ ] Query DB được kiểm tra hiệu năng (index, pagination, limit khi cần).

---

📌 **Ghi chú**

- Không cần ghi chú lại các issue do **SonarLint/Sonar tự động quét**.
- Task được xem là hoàn thành khi **merge vào `main` và CI pass toàn bộ**.

---
📌 **Lưu ý**: Checklist này áp dụng cho mọi User Story/Task trong tất cả Sprint.
