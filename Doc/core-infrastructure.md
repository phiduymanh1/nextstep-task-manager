# Tầng Hạ Tầng Cốt Lõi (Core Infrastructure Layer)

## Tổng quan

Tầng hạ tầng cốt lõi của hệ thống bao gồm các thành phần chính phối hợp với nhau để xử lý request và response một cách nhất quán:

- `ApiResponseUtil`: Lớp tiện ích dùng để tạo response API với metadata chuẩn hóa và dữ liệu trả về.
- `ResponseMetaData`: Đại diện cho metadata của response API, bao gồm trạng thái thành công, mã lỗi, thông báo, thời gian và danh sách lỗi.
- `ApiResponse`: Cấu trúc tổng thể của response API, bao gồm metadata và dữ liệu trả về.
- `GlobalExceptionHandler`: Xử lý ngoại lệ toàn cục và trả về response lỗi theo chuẩn thống nhất.
- `BaseController`: Cung cấp các chức năng dùng chung cho controller, bao gồm xử lý phân trang.

---

## Luồng Request / Response

Luồng xử lý request/response trong hệ thống:

1. Khi một request được gửi đến, nó được tiếp nhận bởi controller.
2. Controller xử lý logic ban đầu và gọi service tương ứng.
3. Service thực hiện nghiệp vụ và trả kết quả về controller.
4. Controller chuyển kết quả sang `ApiResponseUtil` để chuẩn hóa response.
5. `ApiResponseUtil` tạo object `ResponseMetaData` chứa thông tin metadata (success, code, message, timestamp, errors).
6. Sau đó tạo object `ApiResponse` kết hợp metadata và dữ liệu.
7. Controller trả response về client.

---

## Chuẩn hóa API Response

Để đảm bảo tính nhất quán trong toàn hệ thống:

- `ApiResponseUtil` cung cấp các method tạo response chuẩn hóa.
- `ResponseMetaData` chứa thông tin trạng thái và lỗi của response.
- `ApiResponse` là wrapper tổng thể chứa cả metadata và data.
- `ApiResponseUtil` đảm bảo dữ liệu trả về luôn đúng format và thống nhất.

---

## Xử lý Exception Toàn Cục

Luồng xử lý exception:

1. Khi có exception xảy ra, `GlobalExceptionHandler` sẽ bắt lỗi.
2. Xác định loại exception và xử lý phù hợp.
3. Tạo response lỗi thông qua `ApiResponseUtil`.
4. Trả response lỗi về client với HTTP status tương ứng.

---

## Base Controller và Phân Trang

`BaseController` hỗ trợ xử lý các logic chung, đặc biệt là phân trang:

1. Client gửi request kèm thông tin phân trang (page, size).
2. Controller gọi service xử lý dữ liệu.
3. Service trả về dữ liệu đã phân trang.
4. Controller sử dụng `ApiResponseUtil` để đóng gói response.
5. `ResponseMetaData` có thể chứa thêm thông tin pagination.
6. Trả response về client.

---

## Ví dụ cấu trúc API Response

### 1. Success (có dữ liệu)

```json
{
  "meta": {
    "success": true,
    "code": "SUCCESS_CODE",
    "message": "Thành công",
    "timestamp": "2022-01-01T00:00:00",
    "errors": []
  },
  "data": {
    "field1": "value1",
    "field2": "value2"
  }
}
```

### 2. Lỗi đơn (1 message)

```json
{
  "meta": {
    "success": false,
    "code": "ERROR_CODE",
    "message": "Có lỗi xảy ra",
    "timestamp": "2022-01-01T00:00:00",
    "errors": []
  },
  "data": null
}
```

### 3. Lỗi nhiều message

```json
{
  "meta": {
    "success": false,
    "code": "ERROR_CODE",
    "message": "Có nhiều lỗi xảy ra",
    "timestamp": "2022-01-01T00:00:00",
    "errors": ["error1", "error2", "error3"]
  },
  "data": null
}
```

### 4. Lỗi validate

```json
{
  "meta": {
    "success": false,
    "code": "VALIDATE_CODE",
    "message": "Validation failed",
    "timestamp": "2022-01-01T00:00:00",
    "errors": ["error1", "error2", "error3"]
  },
  "data": null
}
```
