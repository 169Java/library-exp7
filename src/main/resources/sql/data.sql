-- ============================================================
-- 实验七 图书管理系统 初始化数据（可重复执行）
-- ============================================================

INSERT IGNORE INTO admin (id, username, password, name) VALUES
(1, 'admin', 'admin123', '系统管理员');

INSERT IGNORE INTO reader (id, username, password, name, phone, email, status) VALUES
(1, 'zhangsan', '123456', '张三', '13800000001', 'zhangsan@example.com', 1),
(2, 'lisi', '123456', '李四', '13800000002', 'lisi@example.com', 1),
(3, 'wangwu', '123456', '王五', '13800000003', 'wangwu@example.com', 0);

INSERT IGNORE INTO book (id, title, author, isbn, publisher, category, price, total_copies, available_copies, description) VALUES
(1, '深入理解计算机系统（原书第3版）', 'Randal E. Bryant', '9787111544937', '机械工业出版社', '计算机', 139.00, 3, 2, '从程序员视角讲解计算机系统，经典教材。'),
(2, 'Java 核心技术 卷I（第12版）', 'Cay S. Horstmann', '9787111636663', '机械工业出版社', '计算机', 149.00, 2, 2, 'Java 语言基础与面向对象编程的权威指南。'),
(3, '算法导论（原书第3版）', 'Thomas H. Cormen', '9787111407010', '机械工业出版社', '计算机', 128.00, 2, 1, '算法设计与分析的经典教材。'),
(4, '代码整洁之道', 'Robert C. Martin', '9787115216878', '人民邮电出版社', '软件工程', 59.00, 4, 4, '如何编写可读、可维护的高质量代码。'),
(5, '计算机网络（第7版）', '谢希仁', '9787121302954', '电子工业出版社', '计算机', 49.00, 5, 5, '国内高校广泛采用的计算机网络教材。'),
(6, '数据库系统概论（第5版）', '王珊', '9787040406641', '高等教育出版社', '计算机', 39.50, 6, 6, '数据库原理与应用的经典教材。'),
(7, '三体（全集）', '刘慈欣', '9787536692930', '重庆出版社', '科幻文学', 99.00, 3, 3, '中国科幻文学的里程碑作品。'),
(8, '活着', '余华', '9787506365437', '作家出版社', '当代文学', 28.00, 5, 5, '讲述一个人一生的苦难与坚韧。'),
(9, '人类简史：从动物到上帝', '尤瓦尔·赫拉利', '9787508647357', '中信出版社', '历史', 68.00, 3, 3, '从认知革命到科学革命的人类发展史。'),
(10, 'Spring 实战（第6版）', 'Craig Walls', '9787115582347', '人民邮电出版社', '计算机', 119.00, 2, 2, 'Spring Boot 应用开发实战指南。');

INSERT IGNORE INTO borrow_record (id, reader_id, book_id, borrow_date, due_date, return_date, status) VALUES
(1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY), NULL, 'BORROWED'),
(2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'RETURNED'),
(3, 1, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 28 DAY), NULL, 'BORROWED');
