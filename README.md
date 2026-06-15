# Secure Employee Management System (SEMS)

Do an giua ky mon Bao mat thong tin.

Muc tieu cua du an la xay dung mot he thong quan ly nguoi dung/nhan vien an toan, tap trung vao cac co che bao mat:

- Dang ky tai khoan.
- Dang nhap bao mat bang BCrypt.
- Xac thuc OTP qua email.
- JWT authentication.
- RBAC voi cac vai tro ADMIN, MANAGER, EMPLOYEE.
- Quan ly nguoi dung: them, sua, xoa, khoa tai khoan, gan role.
- Theo doi lich su dang nhap.
- Audit log cho cac hanh dong quan trong.
- Account lockout va rate limiting de chong brute force.

Frontend chi dong vai tro lop giao dien de demo cac tinh nang bao mat cua backend.

## 1. Cong nghe su dung

Backend:

- Java 21
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT: JJWT 0.12.x
- BCrypt
- Spring Mail
- Swagger/OpenAPI
- Maven Wrapper

Frontend:

- React 18
- Vite
- Axios
- React Router
- Bootstrap
- Lucide React icons

## 2. Yeu cau moi truong

Can cai dat:

- JDK 21
- MySQL Server 8.x
- Node.js 20+ hoac 22+
- Git
- Postman neu muon test API thu cong

Kiem tra nhanh:

```powershell
java -version
node -v
npm -v
git --version
```

## 3. Clone project

Nen clone vao duong dan khong co dau tieng Viet de tranh loi classpath/file lock tren Windows.

Vi du:

```powershell
cd C:\Code
git clone https://github.com/bachiep/BaoMatThongTin.git
cd BaoMatThongTin
```

Neu repo nam trong thu muc con `BaoMatThongTin`, hay mo dung thu muc co file `pom.xml`.

## 4. Cau hinh MySQL

Mac dinh ung dung ket noi toi:

```text
jdbc:mysql://localhost:3306/sems_db
```

Thong tin local demo:

```text
DB_USERNAME=root
DB_PASSWORD=root
```

Ung dung co the tu tao database `sems_db` neu MySQL dang chay va user co quyen tao database.

Neu muon tao database thu cong:

```sql
CREATE DATABASE sems_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Kiem tra MySQL tren Windows:

```powershell
Get-Service -Name MySQL80
```

Neu service dang stopped, start trong Services cua Windows hoac dung PowerShell Admin:

```powershell
Start-Service MySQL80
```

Test ket noi:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "SELECT VERSION();"
```

## 5. Bien moi truong backend

File `.env.example` chi la mau tham khao. Spring Boot trong project nay doc cau hinh tu environment variables hoac cau hinh IDE, khong tu dong doc file `.env`.

Set bien moi truong trong PowerShell truoc khi chay backend:

```powershell
$env:APP_NAME="SEMS"

$env:DB_URL="jdbc:mysql://localhost:3306/sems_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

$env:JWT_SECRET="replace-with-a-long-random-secret-at-least-32-characters"
$env:JWT_EXPIRATION_MINUTES="30"

$env:OTP_EXPIRATION_MINUTES="5"
$env:OTP_LENGTH="6"

$env:LOCKOUT_MAX_FAILED_ATTEMPTS="5"
$env:LOCKOUT_DURATION_MINUTES="15"
$env:LOGIN_RATE_LIMIT_MAX_REQUESTS="5"
$env:LOGIN_RATE_LIMIT_WINDOW_SECONDS="60"

$env:SEED_DATA_ENABLED="true"
$env:SEED_DEFAULT_PASSWORD="Password@123"
```

JWT secret phai dai it nhat 32 ky tu. Khong commit secret that len Git.

## 6. Cau hinh Gmail SMTP cho OTP

OTP email la luong demo chinh cua he thong. Sau khi username/password dung,
backend se sinh OTP, luu vao database va gui OTP toi email cua user. Neu SMTP
chua cau hinh dung hoac Gmail tu choi gui mail, API login se tra loi loi ro rang
thay vi bao thanh cong gia.

De gui OTP qua email that, can Gmail App Password.

Mau cau hinh:

```powershell
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="465"
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_gmail_app_password"
$env:MAIL_FROM="SEMS <your_email@gmail.com>"
$env:MAIL_SMTP_AUTH="true"
$env:MAIL_SMTP_STARTTLS_ENABLE="false"
$env:MAIL_SMTP_SSL_ENABLE="true"
```

Khong dung mat khau Gmail chinh. Hay dung App Password cua Gmail. Khong commit
Gmail App Password len Git.

Tai khoan seed mac dinh dung email local nhu `admin@sems.local`, nen email that co the khong nhan duoc OTP neu chua doi email. Khi demo email that, co the:

- Dang nhap bang admin seed.
- Vao User Management tren frontend.
- Tao mot tai khoan `MANAGER` hoac `EMPLOYEE` voi email Gmail that ma nhom kiem soat.
- Dang xuat admin, dang nhap bang tai khoan moi vua tao.
- Lay OTP trong hop thu Gmail va verify OTP.

Bang `otp_tokens` trong MySQL chi nen dung de doi chieu khi dev hoac khi can
chung minh he thong co luu OTP va thoi gian het han. Khi demo chinh thuc, uu tien
mo hop thu email de xac nhan OTP that.

## 7. Chay backend

Dung JDK 21:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Chay bang Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Hoac build jar:

```powershell
.\mvnw.cmd clean package
java -jar target\sems-0.0.1-SNAPSHOT.jar
```

Backend mac dinh chay tai:

```text
http://localhost:8080
```

Neu gap loi `Could not find or load main class` tren Windows, hay thu:

- Dam bao dang o dung thu muc co `pom.xml`.
- Dam bao dung JDK, khong phai JRE.
- Chuyen project sang duong dan ASCII, vi mot so may Windows loi voi duong dan co dau tieng Viet.
- Chay lai `.\mvnw.cmd clean package`.

## 8. Chay frontend

Mo terminal moi:

```powershell
cd frontend
npm install
npm run dev
```

Frontend chay tai:

```text
http://localhost:5173
```

Vite da cau hinh proxy:

```text
/api -> http://localhost:8080
```

Vi vay backend phai dang chay truoc khi test frontend.

Build frontend:

```powershell
cd frontend
npm run build
```

## 9. Swagger/OpenAPI

Sau khi backend chay, mo:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger dung de xem va test API.

Voi API protected:

1. Login bang `/api/auth/login`.
2. Verify OTP bang `/api/auth/verify-otp`.
3. Copy JWT access token.
4. Bam nut `Authorize` trong Swagger.
5. Nhap:

```text
Bearer <accessToken>
```

Sau do co the test cac API nhu `/api/users`, `/api/employees`, `/api/audit-logs`.

## 10. Tai khoan demo mac dinh

Khi `SEED_DATA_ENABLED=true`, he thong tao cac tai khoan:

| Username | Email | Password | Role |
| --- | --- | --- | --- |
| admin | admin@sems.local | Password@123 | ADMIN |
| manager | manager@sems.local | Password@123 | MANAGER |
| employee | employee@sems.local | Password@123 | EMPLOYEE |

Quyen han:

- ADMIN: toan quyen quan ly user, employee, audit log, login history.
- MANAGER: quan ly employee o muc create/view/edit.
- EMPLOYEE: chi co quyen xem employee.

Frontend login co nut chon nhanh 3 vai tro demo.

## 11. Luong dang nhap bao mat

SEMS dung luong dang nhap 2 buoc:

1. Password login:

```http
POST /api/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "Password@123"
}
```

Neu dung password, server sinh OTP va gui email.

2. Verify OTP:

```http
POST /api/auth/verify-otp
```

Body:

```json
{
  "username": "admin",
  "otp": "123456"
}
```

Neu OTP dung va chua het han, server tra JWT.

3. Goi API protected:

```text
Authorization: Bearer <accessToken>
```

## 12. API chinh

Auth:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/verify-otp
GET  /api/auth/me
```

Users:

```text
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
PATCH  /api/users/{id}/lock
PATCH  /api/users/{id}/unlock
PATCH  /api/users/{id}/roles
```

Employees:

```text
GET    /api/employees
GET    /api/employees/{id}
POST   /api/employees
PUT    /api/employees/{id}
DELETE /api/employees/{id}
```

Monitoring:

```text
GET /api/audit-logs?page=0&size=20
GET /api/login-history?page=0&size=20
```

## 13. Tao them manager/employee tu frontend

Chi ADMIN moi co form tao tai khoan.

Luong thao tac:

1. Dang nhap `admin`.
2. Verify OTP.
3. Vao menu `Users`.
4. Dien username, email, password.
5. Chon role:
   - `MANAGER`
   - `EMPLOYEE`
   - `ADMIN`
6. Bam `Create`.

Password phai dat policy:

- It nhat 8 ky tu.
- Co chu hoa.
- Co chu thuong.
- Co so.
- Co ky tu dac biet.

Vi du hop le:

```text
Password@123
```

## 14. Test backend

Can MySQL dang chay.

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

.\mvnw.cmd -q test
```

## 15. Test frontend

```powershell
cd frontend
npm run build
```

Neu build pass, frontend co the deploy/chay dev binh thuong.

## 16. Cac tinh nang bao mat da co

- BCrypt password hashing.
- Password policy.
- OTP email verification.
- OTP expiration.
- JWT expiration.
- JWT protected API.
- RBAC: ADMIN, MANAGER, EMPLOYEE.
- Permission-based access control.
- Admin user management.
- Account lock/unlock.
- Account lockout sau nhieu lan sai password.
- Rate limiting cho login.
- Login history: username, IP, time, SUCCESS/FAILED.
- Audit log: actor, action, IP, timestamp.
- JSON error response.
- Swagger API documentation.

## 17. Loi thuong gap

### MySQL connection failure

Loi:

```text
Communications link failure
```

Nguyen nhan:

- MySQL chua chay.
- Sai username/password.
- Port 3306 bi dung boi service khac.

Kiem tra:

```powershell
Get-Service -Name MySQL80
```

### No compiler is provided

Nguyen nhan: dang dung JRE thay vi JDK.

Khac phuc: cai JDK 21 va set `JAVA_HOME`.

### Port 8080 da duoc su dung

Kiem tra:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

Dung process neu can:

```powershell
Stop-Process -Id <PID> -Force
```

### Email OTP khong ve

Kiem tra:

- Da dung Gmail App Password chua.
- `MAIL_USERNAME` va `MAIL_PASSWORD` da set dung chua.
- Email cua user co phai email that khong.
- Spam mailbox.

Trong moi truong dev, co the kiem tra bang MySQL:

```sql
SELECT * FROM otp_tokens ORDER BY id DESC;
```

## 18. Luu y Git/security

Khong commit cac file sau:

- `.env`
- `.vscode/`
- Gmail App Password
- JWT secret that
- `target/`
- `node_modules/`
- `frontend/dist/`
- `Do not push github/`

`.gitignore` da cau hinh de loai tru cac file tren.

## 19. Huong demo de xuat

1. Mo frontend.
2. Dang nhap admin.
3. Verify OTP.
4. Dashboard hien role/permission.
5. Admin tao manager/employee.
6. Admin khoa/mo khoa user.
7. Tao/sua/xoa employee.
8. Mo Audit Log de thay hanh dong.
9. Mo Login History de thay SUCCESS/FAILED.
10. Test user thap quyen de thay bi an menu hoac bi 403.
11. Mo Swagger de chung minh API va JWT Bearer auth.

## 20. Pham vi chua lam

Nhung muc sau khong phai trong tam hien tai:

- Quen mat khau/reset password.
- Payroll.
- Cham cong.
- KPI.
- Export PDF.
- Dashboard bieu do nang cao.

Co the ghi cac muc nay vao phan huong phat trien.
