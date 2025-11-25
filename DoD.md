# Definition of Done (DoD) - NextStep Project

> Áp dụng cho mọi User Story/Task trong tất cả Sprint

## Code

- [ ] Code đã compile/build thành công, không lỗi cú pháp hoặc lỗi hệ thống.
- [ ] Tuân thủ coding convention:
  - [ ] Java: camelCase cho biến, PascalCase cho class, tách rõ Controller/Service/Repository.
  - [ ] Vue: đặt tên component đúng chuẩn, không để logic phức tạp trong template.
- [ ] Không còn đoạn code thừa, comment rác, `console.log` hoặc `System.out.println` chưa xóa.
- [ ] Đã chạy formatter/linter (Prettier, ESLint, Checkstyle...) nếu có.
- [ ] Có unit test cho các hàm xử lý quan trọng (nếu cần).
- [ ] API đã được test qua Postman hoặc công cụ tương đương.
- [ ] Code đã được push lên branch `develop` và được review xong.

## Database

- [ ] Nếu có thay đổi DB, script SQL đã được cập nhật trong thư mục `/db`.
- [ ] Migration đã chạy thành công trên môi trường local (hoặc staging).
- [ ] Cấu trúc DB phản ánh đúng yêu cầu của User Story.
- [ ] Không ảnh hưởng đến dữ liệu cũ hoặc gây lỗi khi truy vấn.

## Document

- [ ] User Story/Task đã được cập nhật trạng thái trong Product Backlog.
- [ ] Có mô tả API rõ ràng:
  - [ ] Trong Postman Collection hoặc file Markdown.
  - [ ] Ghi rõ endpoint, method, request, response, status code.
- [ ] Nếu có logic phức tạp, đã được comment hoặc ghi chú trong code.

## Test

- [ ] Manual test đã được thực hiện đầy đủ các luồng chính và edge case.
- [ ] Không còn bug/blocker ở feature vừa làm.
- [ ] Nếu có bug, đã ghi nhận hoặc xử lý trong Sprint.
- [ ] Đã test tương thích trình duyệt (Chrome, Firefox...) nếu là frontend.
- [ ] Nếu có test tự động, đã chạy và pass toàn bộ.

## Deployment

- [ ] Build chạy OK ở local (Spring Boot + Vue).
- [ ] App đã được chạy thử sau build để kiểm tra UI và API hoạt động đúng.
- [ ] Không có lỗi console hoặc warning nghiêm trọng khi chạy app.
- [ ] Commit có message rõ ràng, mô tả đúng nội dung thay đổi.


## 🔐 Optional – Security & Performance

- [ ] Không có dữ liệu nhạy cảm hard-code trong code.
- [ ] Đã kiểm tra các API không bị lỗi bảo mật cơ bản (SQL Injection, CORS...).
- [ ] Nếu có query DB, đã kiểm tra hiệu năng (index, limit, pagination...).
---
📌 **Lưu ý**: Checklist này áp dụng cho mọi User Story/Task trong tất cả Sprint.
