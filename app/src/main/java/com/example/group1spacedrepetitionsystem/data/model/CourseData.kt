package com.example.estudapp.data.model

data class CourseDTO(
    val id: String,
    val name: String,
    val description: String,
    val flashcards: List<FlashcardDTO>
)

object CourseData {
    val courses = listOf(
        CourseDTO(
            id = "course_tin_hoc",
            name = "Tin học đại cương",
            description = "Kiến thức cơ bản về máy tính và hệ điều hành",
            flashcards = listOf(
                // Cơ bản
                FlashcardDTO(id = "th1", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "RAM là viết tắt của cụm từ gì trong tiếng Anh?", verso = "Random Access Memory"),
                FlashcardDTO(id = "th2", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Đơn vị nhỏ nhất để biểu diễn thông tin trong máy tính là gì?", verso = "Bit"),
                FlashcardDTO(id = "th3", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Hệ điều hành là gì?", verso = "Phần mềm quản lý tài nguyên máy tính và làm cầu nối giữa người dùng và phần cứng."),
                FlashcardDTO(id = "th4", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Phần mềm nào dùng để duyệt web?", verso = "Trình duyệt web (Chrome, Firefox, Edge,...)"),
                FlashcardDTO(id = "th5", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "CPU là viết tắt của từ gì?", verso = "Central Processing Unit"),
                
                // Nhập liệu
                FlashcardDTO(id = "th11", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Hệ đếm thập phân gồm các chữ số từ mấy đến mấy?", respostasValidas = listOf("0 đến 9"), verso = ""),
                FlashcardDTO(id = "th12", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Tên của thiết bị hiển thị thông tin ra màn hình?", respostasValidas = listOf("Màn hình", "Monitor"), verso = ""),
                FlashcardDTO(id = "th13", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Phần mềm soạn thảo văn bản phổ biến của Microsoft?", respostasValidas = listOf("Word"), verso = ""),
                FlashcardDTO(id = "th14", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "1MB bằng bao nhiêu KB?", respostasValidas = listOf("1024"), verso = ""),
                FlashcardDTO(id = "th15", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Tên của bộ nhớ chỉ đọc?", respostasValidas = listOf("ROM"), verso = ""),
                
                // Trắc nghiệm
                FlashcardDTO(id = "th16", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Thiết bị nào sau đây là thiết bị lưu trữ?", 
                    alternativas = listOf(AlternativaDTO("Màn hình"), AlternativaDTO("Chuột"), AlternativaDTO("Ổ cứng"), AlternativaDTO("Máy in")),
                    respostaCorreta = AlternativaDTO("Ổ cứng"), verso = ""),
                FlashcardDTO(id = "th17", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Hệ đếm thập phân sử dụng bao nhiêu chữ số?", 
                    alternativas = listOf(AlternativaDTO("2"), AlternativaDTO("8"), AlternativaDTO("10"), AlternativaDTO("16")),
                    respostaCorreta = AlternativaDTO("10"), verso = ""),
                FlashcardDTO(id = "th18", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Phần mềm nào là hệ điều hành?", 
                    alternativas = listOf(AlternativaDTO("Word"), AlternativaDTO("Excel"), AlternativaDTO("Windows"), AlternativaDTO("Photoshop")),
                    respostaCorreta = AlternativaDTO("Windows"), verso = ""),
                FlashcardDTO(id = "th19", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "1 GB bằng bao nhiêu MB?", 
                    alternativas = listOf(AlternativaDTO("100"), AlternativaDTO("512"), AlternativaDTO("1000"), AlternativaDTO("1024")),
                    respostaCorreta = AlternativaDTO("1024"), verso = ""),
                FlashcardDTO(id = "th20", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Chức năng chính của bộ xử lý trung tâm (CPU) là gì?", 
                    alternativas = listOf(AlternativaDTO("Lưu trữ dữ liệu lâu dài"), AlternativaDTO("Hiển thị hình ảnh"), AlternativaDTO("Xử lý lệnh và tính toán"), AlternativaDTO("Kết nối Internet")),
                    respostaCorreta = AlternativaDTO("Xử lý lệnh và tính toán"), verso = "")
            )
        ),
        CourseDTO(
            id = "course_lap_trinh_di_dong",
            name = "Lập trình di động",
            description = "Phát triển ứng dụng cho Android và iOS",
            flashcards = listOf(
                // Cơ bản
                FlashcardDTO(id = "dd1", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Android sử dụng ngôn ngữ chính nào để phát triển ứng dụng?", verso = "Java / Kotlin"),
                FlashcardDTO(id = "dd2", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "iOS sử dụng ngôn ngữ nào chính thức?", verso = "Swift"),
                FlashcardDTO(id = "dd3", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Tên của tệp cấu hình chính trong Android Studio là gì?", verso = "AndroidManifest.xml"),
                FlashcardDTO(id = "dd4", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Flutter sử dụng ngôn ngữ nào?", verso = "Dart"),
                FlashcardDTO(id = "dd5", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Giao diện người dùng trong Android được xây dựng bằng tệp mở rộng gì?", verso = ".xml"),
                
                // Nhập liệu
                FlashcardDTO(id = "dd11", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Tên ngôn ngữ thay thế Java trên Android được Google khuyến nghị?", respostasValidas = listOf("Kotlin"), verso = ""),
                FlashcardDTO(id = "dd12", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Lệnh khởi tạo project Flutter?", respostasValidas = listOf("flutter create"), verso = ""),
                FlashcardDTO(id = "dd13", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Hệ điều hành di động của Apple?", respostasValidas = listOf("iOS"), verso = ""),
                FlashcardDTO(id = "dd14", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Công cụ build chính trong Android Studio?", respostasValidas = listOf("Gradle"), verso = ""),
                FlashcardDTO(id = "dd15", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Kiến trúc phổ biến trong Android hiện nay?", respostasValidas = listOf("MVVM"), verso = ""),
                
                // Trắc nghiệm
                FlashcardDTO(id = "dd16", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Ngôn ngữ nào dùng để phát triển ứng dụng Android chính thức?", 
                    alternativas = listOf(AlternativaDTO("Swift"), AlternativaDTO("Objective-C"), AlternativaDTO("Kotlin"), AlternativaDTO("C#")),
                    respostaCorreta = AlternativaDTO("Kotlin"), verso = ""),
                FlashcardDTO(id = "dd17", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Để yêu cầu quyền truy cập camera trong Android, cần thêm gì vào AndroidManifest.xml?", 
                    alternativas = listOf(AlternativaDTO("<uses-camera>"), AlternativaDTO("<permission-camera>"), AlternativaDTO("<uses-permission android:name=\"...CAMERA\" />"), AlternativaDTO("<request-camera />")),
                    respostaCorreta = AlternativaDTO("<uses-permission android:name=\"...CAMERA\" />"), verso = ""),
                FlashcardDTO(id = "dd18", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Flutter do công ty nào phát triển?", 
                    alternativas = listOf(AlternativaDTO("Apple"), AlternativaDTO("Microsoft"), AlternativaDTO("Google"), AlternativaDTO("Facebook")),
                    respostaCorreta = AlternativaDTO("Google"), verso = ""),
                FlashcardDTO(id = "dd19", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Trong React Native, thành phần nào dùng để tạo vùng cuộn?", 
                    alternativas = listOf(AlternativaDTO("View"), AlternativaDTO("ScrollView"), AlternativaDTO("FlatList"), AlternativaDTO("Text")),
                    respostaCorreta = AlternativaDTO("ScrollView"), verso = ""),
                FlashcardDTO(id = "dd20", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Kiến trúc phổ biến trong Android hiện nay được Google khuyến nghị?", 
                    alternativas = listOf(AlternativaDTO("MVC"), AlternativaDTO("MVP"), AlternativaDTO("MVVM"), AlternativaDTO("MVI")),
                    respostaCorreta = AlternativaDTO("MVVM"), verso = "")
            )
        ),
        CourseDTO(
            id = "course_javascript",
            name = "JavaScript",
            description = "Lập trình web với JavaScript",
            flashcards = listOf(
                // Cơ bản
                FlashcardDTO(id = "js1", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "JavaScript có quan hệ gì với Java?", verso = "Không liên quan, chỉ tên giống nhau"),
                FlashcardDTO(id = "js2", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Dùng từ khóa nào để khai báo biến trong JavaScript hiện đại?", verso = "let, const"),
                FlashcardDTO(id = "js3", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "typeof null trả về kiểu gì?", verso = "\"object\""),
                FlashcardDTO(id = "js4", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Làm thế nào để in ra màn hình console trong JS?", verso = "console.log()"),
                FlashcardDTO(id = "js5", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Hàm setTimeout dùng để làm gì?", verso = "Thực thi code sau một khoảng thời gian trễ"),
                
                // Nhập liệu
                FlashcardDTO(id = "js11", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Cách khai báo biến không thể gán lại?", respostasValidas = listOf("const"), verso = ""),
                FlashcardDTO(id = "js12", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Lệnh dùng để lặp qua các thuộc tính của object?", respostasValidas = listOf("for...in"), verso = ""),
                FlashcardDTO(id = "js13", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Hàm chạy sau 1 khoảng thời gian lặp đi lặp lại?", respostasValidas = listOf("setInterval"), verso = ""),
                FlashcardDTO(id = "js14", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Kiểu dữ liệu của NaN?", respostasValidas = listOf("number"), verso = ""),
                FlashcardDTO(id = "js15", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Toán tử cộng chuỗi trong JS?", respostasValidas = listOf("+"), verso = ""),
                
                // Trắc nghiệm
                FlashcardDTO(id = "js16", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Kết quả của \"5\" + 3 trong JavaScript?", 
                    alternativas = listOf(AlternativaDTO("8"), AlternativaDTO("\"53\""), AlternativaDTO("53"), AlternativaDTO("NaN")),
                    respostaCorreta = AlternativaDTO("\"53\""), verso = ""),
                FlashcardDTO(id = "js17", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Làm thế nào để tạo một mảng trong JS?", 
                    alternativas = listOf(AlternativaDTO("{}"), AlternativaDTO("[]"), AlternativaDTO("<>"), AlternativaDTO("()")),
                    respostaCorreta = AlternativaDTO("[]"), verso = ""),
                FlashcardDTO(id = "js18", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Phương thức nào dùng để thêm phần tử vào cuối mảng?", 
                    alternativas = listOf(AlternativaDTO("push()"), AlternativaDTO("pop()"), AlternativaDTO("shift()"), AlternativaDTO("unshift()")),
                    respostaCorreta = AlternativaDTO("push()"), verso = ""),
                FlashcardDTO(id = "js19", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "setInterval khác setTimeout ở điểm nào?", 
                    alternativas = listOf(AlternativaDTO("Không khác"), AlternativaDTO("setInterval chạy lặp lại"), AlternativaDTO("setTimeout chạy lặp lại"), AlternativaDTO("setInterval chỉ chạy một lần")),
                    respostaCorreta = AlternativaDTO("setInterval chạy lặp lại"), verso = ""),
                FlashcardDTO(id = "js20", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Cú pháp nào đúng để khai báo hàm mũi tên?", 
                    alternativas = listOf(AlternativaDTO("function() => {}"), AlternativaDTO("=> function() {}"), AlternativaDTO("() => {}"), AlternativaDTO("() => function()")),
                    respostaCorreta = AlternativaDTO("() => {}"), verso = "")
            )
        ),
        CourseDTO(
            id = "course_sql",
            name = "Cơ sở dữ liệu",
            description = "Ngôn ngữ truy vấn cấu trúc SQL và CSDL",
            flashcards = listOf(
                // Cơ bản
                FlashcardDTO(id = "sql1", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "SQL là viết tắt của cụm từ gì?", verso = "Structured Query Language"),
                FlashcardDTO(id = "sql2", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Khóa chính (Primary Key) dùng để làm gì?", verso = "Xác định duy nhất mỗi bản ghi trong bảng"),
                FlashcardDTO(id = "sql3", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Câu lệnh lấy tất cả dữ liệu từ bảng \"SinhVien\"?", verso = "SELECT * FROM SinhVien"),
                FlashcardDTO(id = "sql4", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Khóa ngoại (Foreign Key) dùng để làm gì?", verso = "Liên kết giữa hai bảng"),
                FlashcardDTO(id = "sql5", type = FlashcardTypeEnum.FRENTE_VERSO.name, frente = "Hệ quản trị CSDL quan hệ phổ biến miễn phí?", verso = "MySQL, PostgreSQL"),
                
                // Nhập liệu
                FlashcardDTO(id = "sql11", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Câu lệnh thêm cột mới vào bảng đã có?", respostasValidas = listOf("ALTER TABLE ... ADD"), verso = ""),
                FlashcardDTO(id = "sql12", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Kiểu dữ liệu lưu chuỗi dài trong MySQL?", respostasValidas = listOf("TEXT"), verso = ""),
                FlashcardDTO(id = "sql13", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Hàm lấy năm từ ngày tháng trong SQL?", respostasValidas = listOf("YEAR()"), verso = ""),
                FlashcardDTO(id = "sql14", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Câu lệnh xóa toàn bộ dữ liệu nhưng giữ cấu trúc bảng?", respostasValidas = listOf("TRUNCATE"), verso = ""),
                FlashcardDTO(id = "sql15", type = FlashcardTypeEnum.DIGITE_RESPOSTA.name, pergunta = "Tên ràng buộc đảm bảo giá trị duy nhất không trùng?", respostasValidas = listOf("UNIQUE"), verso = ""),
                
                // Trắc nghiệm
                FlashcardDTO(id = "sql16", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Lệnh nào dùng để thêm dữ liệu vào bảng?", 
                    alternativas = listOf(AlternativaDTO("INSERT INTO"), AlternativaDTO("ADD INTO"), AlternativaDTO("UPDATE"), AlternativaDTO("SELECT")),
                    respostaCorreta = AlternativaDTO("INSERT INTO"), verso = ""),
                FlashcardDTO(id = "sql17", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Loại JOIN nào trả về các bản ghi có ở cả hai bảng?", 
                    alternativas = listOf(AlternativaDTO("LEFT JOIN"), AlternativaDTO("RIGHT JOIN"), AlternativaDTO("INNER JOIN"), AlternativaDTO("FULL JOIN")),
                    respostaCorreta = AlternativaDTO("INNER JOIN"), verso = ""),
                FlashcardDTO(id = "sql18", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Lệnh xóa bảng nhưng giữ cấu trúc (xóa dữ liệu)?", 
                    alternativas = listOf(AlternativaDTO("DELETE"), AlternativaDTO("DROP"), AlternativaDTO("TRUNCATE"), AlternativaDTO("REMOVE")),
                    respostaCorreta = AlternativaDTO("TRUNCATE"), verso = ""),
                FlashcardDTO(id = "sql19", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Chỉ mục (Index) dùng để làm gì?", 
                    alternativas = listOf(AlternativaDTO("Giảm tốc độ truy vấn"), AlternativaDTO("Tăng tốc độ tìm kiếm"), AlternativaDTO("Xóa dữ liệu"), AlternativaDTO("Sao lưu dữ liệu")),
                    respostaCorreta = AlternativaDTO("Tăng tốc độ tìm kiếm"), verso = ""),
                FlashcardDTO(id = "sql20", type = FlashcardTypeEnum.MULTIPLA_ESCOLHA.name, pergunta = "Dạng chuẩn 1 (1NF) yêu cầu gì?", 
                    alternativas = listOf(AlternativaDTO("Không có phụ thuộc hàm"), AlternativaDTO("Không có nhóm lặp"), AlternativaDTO("Không có khóa chính"), AlternativaDTO("Có khóa ngoại")),
                    respostaCorreta = AlternativaDTO("Không có nhóm lặp"), verso = "")
            )
        )
    )
}
