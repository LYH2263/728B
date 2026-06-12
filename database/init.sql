-- Steam 类游戏平台数据库初始化脚本
-- 字符集 UTF8MB4 支持中文和emoji

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT '/avatars/default.png' COMMENT '头像URL',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    `role` ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '角色',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 游戏分类表
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) COMMENT '分类描述',
    `icon` VARCHAR(200) COMMENT '分类图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏分类表';

-- 游戏表
CREATE TABLE IF NOT EXISTS `games` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '游戏名称',
    `description` TEXT COMMENT '游戏简介',
    `detail_description` TEXT COMMENT '详细介绍',
    `cover_image` VARCHAR(500) COMMENT '封面图片',
    `banner_image` VARCHAR(500) COMMENT '横幅图片',
    `screenshots` JSON COMMENT '游戏截图JSON数组',
    `video_url` VARCHAR(500) COMMENT '预告片URL',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `discount_price` DECIMAL(10,2) COMMENT '折扣价',
    `discount_percent` INT DEFAULT 0 COMMENT '折扣百分比',
    `developer` VARCHAR(100) COMMENT '开发商',
    `publisher` VARCHAR(100) COMMENT '发行商',
    `release_date` DATE COMMENT '发售日期',
    `min_requirements` JSON COMMENT '最低配置要求',
    `rec_requirements` JSON COMMENT '推荐配置要求',
    `tags` JSON COMMENT '标签',
    `stock` INT DEFAULT 9999 COMMENT '库存',
    `sales_count` INT DEFAULT 0 COMMENT '销量',
    `rating` DECIMAL(2,1) DEFAULT 0.0 COMMENT '评分(0-5)',
    `rating_count` INT DEFAULT 0 COMMENT '评分人数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0下架 1上架',
    `is_featured` TINYINT DEFAULT 0 COMMENT '是否精选推荐',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_title` (`title`),
    INDEX `idx_status` (`status`),
    INDEX `idx_featured` (`is_featured`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏表';

-- 游戏分类关联表
CREATE TABLE IF NOT EXISTS `game_categories` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `game_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_game_category` (`game_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏分类关联表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart_items` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `game_id` BIGINT NOT NULL,
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_game` (`user_id`, `game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 愿望单表
CREATE TABLE IF NOT EXISTS `wishlist` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `game_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_game_wish` (`user_id`, `game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='愿望单表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL,
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `status` ENUM('PENDING', 'PAID', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING' COMMENT '订单状态',
    `pay_time` DATETIME COMMENT '支付时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `game_id` BIGINT NOT NULL,
    `game_title` VARCHAR(200) NOT NULL COMMENT '游戏名称(冗余)',
    `game_cover` VARCHAR(500) COMMENT '游戏封面(冗余)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '成交价格',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 用户游戏库(已购买的游戏)
CREATE TABLE IF NOT EXISTS `user_library` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `game_id` BIGINT NOT NULL,
    `order_id` BIGINT COMMENT '关联订单',
    `play_time` INT DEFAULT 0 COMMENT '游玩时长(分钟)',
    `last_played_at` DATETIME COMMENT '最后游玩时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`),
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),
    UNIQUE KEY `uk_user_game_lib` (`user_id`, `game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户游戏库';

-- 游戏评论表
CREATE TABLE IF NOT EXISTS `game_reviews` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `game_id` BIGINT NOT NULL,
    `rating` INT NOT NULL COMMENT '评分(1-5)',
    `content` TEXT COMMENT '评论内容',
    `is_recommend` TINYINT DEFAULT 1 COMMENT '是否推荐',
    `helpful_count` INT DEFAULT 0 COMMENT '有帮助数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_game_review` (`user_id`, `game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏评论表';

-- ===================== 初始化演示数据 =====================

-- 插入管理员和测试用户 (密码都是 123456，使用BCrypt加密)
INSERT INTO `users` (`username`, `password`, `email`, `nickname`, `avatar`, `balance`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@steam.com', '管理员', '/avatars/admin.png', 10000.00, 'ADMIN'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'test@steam.com', '测试玩家', '/avatars/default.png', 500.00, 'USER'),
('gamer001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'gamer@steam.com', '游戏达人', '/avatars/default.png', 1200.00, 'USER');

-- 插入游戏分类
INSERT INTO `categories` (`name`, `description`, `icon`, `sort_order`) VALUES
('动作游戏', '以动作为主的游戏类型', '🎮', 1),
('角色扮演', 'RPG角色扮演游戏', '🧙', 2),
('射击游戏', 'FPS/TPS射击类游戏', '🔫', 3),
('策略游戏', '需要策略思考的游戏', '♟️', 4),
('冒险游戏', '探索冒险类游戏', '🗺️', 5),
('模拟经营', '模拟经营类游戏', '🏗️', 6),
('体育竞技', '体育运动类游戏', '⚽', 7),
('独立游戏', '独立开发者制作的游戏', '💎', 8),
('免费游戏', '免费畅玩的游戏', '🆓', 9),
('多人在线', '支持多人在线的游戏', '👥', 10);

-- 插入游戏数据
INSERT INTO `games` (`title`, `description`, `detail_description`, `cover_image`, `banner_image`, `screenshots`, `original_price`, `discount_price`, `discount_percent`, `developer`, `publisher`, `release_date`, `min_requirements`, `rec_requirements`, `tags`, `stock`, `sales_count`, `rating`, `rating_count`, `is_featured`) VALUES
('赛博朋克 2077', '在赛博朋克2077中，您将在夜之城这座权力、魅力和改造身体永无止境的巨型都市中闯荡。', '《赛博朋克2077》是一款开放世界动作冒险RPG，故事发生在夜之城——一座痴迷于权力、魅力和改造身体的巨型都市。您将扮演一名雇佣兵V，追寻一种独一无二的植入体——获得永生的关键。自定义您的角色的改造机体、技能和游戏风格，在一座依靠实力说话的城市中探索一个开放世界。', 
'/game-assets/cyberpunk-2077/cover.jpg', 
'/game-assets/cyberpunk-2077/banner.jpg',
'["/game-assets/cyberpunk-2077/screenshots/1.jpg", "/game-assets/cyberpunk-2077/screenshots/2.jpg"]',
298.00, 149.00, 50, 'CD PROJEKT RED', 'CD PROJEKT RED', '2020-12-10',
'{"os": "Windows 10", "cpu": "Intel Core i5-3570K", "memory": "8 GB RAM", "gpu": "NVIDIA GeForce GTX 970", "storage": "70 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i7-4790", "memory": "16 GB RAM", "gpu": "NVIDIA GeForce RTX 2060", "storage": "70 GB SSD"}',
'["开放世界", "RPG", "赛博朋克", "未来", "动作"]',
999, 15680, 4.5, 28540, 1),

('艾尔登法环', '崭新的奇幻动作RPG。在辽阔的场景与地下迷宫的交织中踏上冒险，体验未知与压倒性的游戏成就感。', '《艾尔登法环》是由FromSoftware开发，万代南梦宫发行的动作角色扮演游戏。本作由宫崎英高担任导演，乔治·R·R·马丁负责世界观设定。游戏以辽阔的世界观与深度的角色扮演体验闻名。',
'/game-assets/elden-ring/cover.jpg',
'/game-assets/elden-ring/banner.jpg',
'["/game-assets/elden-ring/screenshots/1.jpg", "/game-assets/elden-ring/screenshots/2.jpg"]',
298.00, 223.50, 25, 'FromSoftware Inc.', 'BANDAI NAMCO', '2022-02-25',
'{"os": "Windows 10", "cpu": "Intel Core i5-8400", "memory": "12 GB RAM", "gpu": "NVIDIA GeForce GTX 1060", "storage": "60 GB"}',
'{"os": "Windows 11", "cpu": "Intel Core i7-8700K", "memory": "16 GB RAM", "gpu": "NVIDIA GeForce RTX 3060", "storage": "60 GB SSD"}',
'["魂系游戏", "开放世界", "动作", "困难", "黑暗奇幻"]',
999, 22350, 4.8, 45230, 1),

('霍格沃茨之遗', '体验19世纪霍格沃茨的生活。您的角色是掌握古老秘密的关键，它将威胁到整个魔法世界。', '《霍格沃茨之遗》是一款沉浸式开放世界动作RPG游戏，故事背景设定在《哈利波特》系列书籍中首次介绍的魔法世界，让玩家能够以前所未有的方式掌控冒险旅程。',
'/game-assets/hogwarts-legacy/cover.jpg',
'/game-assets/hogwarts-legacy/banner.jpg',
'["/game-assets/hogwarts-legacy/screenshots/1.jpg", "/game-assets/hogwarts-legacy/screenshots/2.jpg"]',
298.00, 238.40, 20, 'Avalanche Software', 'Warner Bros. Games', '2023-02-10',
'{"os": "Windows 10", "cpu": "Intel Core i5-6600", "memory": "16 GB RAM", "gpu": "NVIDIA GeForce GTX 960", "storage": "85 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i7-8700", "memory": "32 GB RAM", "gpu": "NVIDIA GeForce RTX 2080", "storage": "85 GB SSD"}',
'["魔法", "开放世界", "冒险", "RPG", "哈利波特"]',
999, 18920, 4.6, 32100, 1),

('博德之门3', '召集同伴，重返被遗忘的国度。开启一段讲述友情与背叛、牺牲与生存的传奇故事。', '《博德之门3》是一款以传奇RPG游戏为基础的角色扮演游戏，采用了D&D规则集，玩家将在被遗忘的国度展开冒险。由Larian Studios开发制作。',
'/game-assets/baldurs-gate-3/cover.jpg',
'/game-assets/baldurs-gate-3/banner.jpg',
'["/game-assets/baldurs-gate-3/screenshots/1.jpg", "/game-assets/baldurs-gate-3/screenshots/2.jpg"]',
298.00, NULL, 0, 'Larian Studios', 'Larian Studios', '2023-08-03',
'{"os": "Windows 10", "cpu": "Intel Core i5-4690", "memory": "8 GB RAM", "gpu": "NVIDIA GeForce GTX 970", "storage": "150 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i7-8700K", "memory": "16 GB RAM", "gpu": "NVIDIA GeForce RTX 2060", "storage": "150 GB SSD"}',
'["回合制", "RPG", "D&D", "剧情丰富", "多人"]',
999, 25600, 4.9, 58900, 1),

('CS2 反恐精英2', '在超过25年的时间中，CS已经提供了精英级别的竞技体验，是世界上最热门的射击游戏之一。', '《反恐精英2》是Valve开发的免费多人第一人称射击游戏，是CS:GO的继任者。游戏继承了经典的炸弹模式和人质模式，带来了全新的渲染技术和游戏体验。',
'/game-assets/counter-strike-2/cover.jpg',
'/game-assets/counter-strike-2/banner.jpg',
'["/game-assets/counter-strike-2/screenshots/1.jpg", "/game-assets/counter-strike-2/screenshots/2.jpg"]',
0.00, NULL, 0, 'Valve', 'Valve', '2023-09-27',
'{"os": "Windows 10", "cpu": "Intel Core i5-4460", "memory": "8 GB RAM", "gpu": "NVIDIA GeForce GTX 750 Ti", "storage": "85 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i7-9700K", "memory": "16 GB RAM", "gpu": "NVIDIA GeForce RTX 2060", "storage": "85 GB SSD"}',
'["FPS", "射击", "竞技", "多人", "免费"]',
9999, 156000, 4.3, 125000, 1),

('只狼：影逝二度', '《只狼：影逝二度》是FromSoftware开发的动作冒险游戏，讲述了忍者与武士的故事。', '《只狼：影逝二度》是一款以日本战国时代为背景的动作冒险游戏。您是一位残废的战士，在拯救您的年轻主人并向敌人复仇的过程中，将与众多凶恶的敌人进行殊死搏斗。',
'/game-assets/sekiro-shadows-die-twice/cover.jpg',
'/game-assets/sekiro-shadows-die-twice/banner.jpg',
'["/game-assets/sekiro-shadows-die-twice/screenshots/1.jpg", "/game-assets/sekiro-shadows-die-twice/screenshots/2.jpg"]',
268.00, 134.00, 50, 'FromSoftware', 'Activision', '2019-03-22',
'{"os": "Windows 7", "cpu": "Intel Core i3-2100", "memory": "4 GB RAM", "gpu": "NVIDIA GeForce GTX 760", "storage": "25 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i5-2500K", "memory": "8 GB RAM", "gpu": "NVIDIA GeForce GTX 970", "storage": "25 GB SSD"}',
'["动作", "忍者", "困难", "魂系游戏", "日本"]',
999, 12500, 4.7, 28600, 0),

('文明6', '文明6提供了多种新方式让您与世界互动、拓展帝国、推进文化并与历史上的伟大领袖竞争，缔造经得起时间考验的帝国。', '《席德·梅尔的文明6》是著名的4X回合制策略游戏，玩家将从石器时代带领自己的文明发展到信息时代。',
'/game-assets/civilization-6/cover.jpg',
'/game-assets/civilization-6/banner.jpg',
'["/game-assets/civilization-6/screenshots/1.jpg", "/game-assets/civilization-6/screenshots/2.jpg"]',
199.00, 49.75, 75, 'Firaxis Games', '2K', '2016-10-21',
'{"os": "Windows 7", "cpu": "Intel Core i3-2.5GHz", "memory": "4 GB RAM", "gpu": "NVIDIA GeForce 450", "storage": "12 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i5-4460", "memory": "8 GB RAM", "gpu": "NVIDIA GeForce GTX 770", "storage": "12 GB"}',
'["策略", "回合制", "4X", "历史", "多人"]',
999, 35600, 4.4, 42300, 0),

('星露谷物语', '你继承了爷爷在星露谷留下的老旧农场。带着祖传的工具和几个钱币，你出发开始了新生活。', '《星露谷物语》是一款开放式乡村生活模拟游戏！你将经营一个农场，学习种植庄稼、饲养家畜、发展手工业、与村民交朋友、结婚并组建家庭。',
'/game-assets/stardew-valley/cover.jpg',
'/game-assets/stardew-valley/banner.jpg',
'["/game-assets/stardew-valley/screenshots/1.jpg", "/game-assets/stardew-valley/screenshots/2.jpg"]',
48.00, 33.60, 30, 'ConcernedApe', 'ConcernedApe', '2016-02-26',
'{"os": "Windows Vista", "cpu": "2GHz", "memory": "2 GB RAM", "gpu": "256 MB Video Memory", "storage": "500 MB"}',
'{"os": "Windows 10", "cpu": "2GHz", "memory": "4 GB RAM", "gpu": "512 MB Video Memory", "storage": "500 MB"}',
'["农场", "模拟", "像素", "放松", "独立"]',
999, 68000, 4.9, 95600, 0),

('黑神话：悟空', '一款以中国神话为背景的动作角色扮演游戏。探索古老传说中的秘密，并与危险的敌人战斗。', '《黑神话：悟空》是由游戏科学开发的单机动作角色扮演游戏，以中国古典小说《西游记》为故事背景，讲述了一段全新的西游故事。',
'/game-assets/black-myth-wukong/cover.jpg',
'/game-assets/black-myth-wukong/banner.jpg',
'["/game-assets/black-myth-wukong/screenshots/1.jpg", "/game-assets/black-myth-wukong/screenshots/2.jpg"]',
268.00, NULL, 0, 'Game Science', 'Game Science', '2024-08-20',
'{"os": "Windows 10", "cpu": "Intel Core i5-8400", "memory": "16 GB RAM", "gpu": "NVIDIA GeForce GTX 1060", "storage": "130 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i7-9700", "memory": "32 GB RAM", "gpu": "NVIDIA GeForce RTX 3060", "storage": "130 GB SSD"}',
'["动作", "中国神话", "西游记", "单人", "剧情"]',
999, 89000, 4.8, 78500, 1),

('荒野大镖客2', '《荒野大镖客：救赎2》是史诗般的西部冒险故事，游戏背景设在美国的1899年，讲述了亚瑟·摩根的故事。', '《荒野大镖客：救赎2》是Rockstar Games的一款西部主题动作冒险游戏，被誉为游戏史上最伟大的作品之一。',
'/game-assets/red-dead-redemption-2/cover.jpg',
'/game-assets/red-dead-redemption-2/banner.jpg',
'["/game-assets/red-dead-redemption-2/screenshots/1.jpg", "/game-assets/red-dead-redemption-2/screenshots/2.jpg"]',
279.00, 111.60, 60, 'Rockstar Games', 'Rockstar Games', '2019-12-05',
'{"os": "Windows 7", "cpu": "Intel Core i5-2500K", "memory": "8 GB RAM", "gpu": "NVIDIA GeForce GTX 770", "storage": "150 GB"}',
'{"os": "Windows 10", "cpu": "Intel Core i7-4770K", "memory": "12 GB RAM", "gpu": "NVIDIA GeForce GTX 1060", "storage": "150 GB"}',
'["开放世界", "西部", "冒险", "故事丰富", "马匹"]',
999, 42000, 4.7, 56800, 0);

-- 关联游戏和分类
INSERT INTO `game_categories` (`game_id`, `category_id`) VALUES
(1, 1), (1, 2), (1, 5),   -- 赛博朋克: 动作、RPG、冒险
(2, 1), (2, 2),            -- 艾尔登法环: 动作、RPG
(3, 2), (3, 5),            -- 霍格沃茨: RPG、冒险
(4, 2), (4, 10),           -- 博德之门3: RPG、多人
(5, 3), (5, 9), (5, 10),   -- CS2: 射击、免费、多人
(6, 1), (6, 5),            -- 只狼: 动作、冒险
(7, 4), (7, 10),           -- 文明6: 策略、多人
(8, 6), (8, 8),            -- 星露谷: 模拟、独立
(9, 1), (9, 2),            -- 黑神话: 动作、RPG
(10, 1), (10, 5);          -- 荒野大镖客: 动作、冒险

-- 添加一些示例评论
INSERT INTO `game_reviews` (`user_id`, `game_id`, `rating`, `content`, `is_recommend`, `helpful_count`) VALUES
(2, 1, 5, '夜之城真的太美了！剧情和角色都非常出色，强烈推荐！', 1, 125),
(3, 1, 4, '游戏很好玩，但是优化还有提升空间。', 1, 56),
(2, 2, 5, '魂系游戏的巅峰之作，开放世界设计太棒了！', 1, 230),
(3, 2, 5, '难度适中，探索感十足，年度最佳游戏。', 1, 180),
(2, 4, 5, 'D&D规则的完美呈现，剧情分支超多，每次玩都有新体验。', 1, 320),
(2, 9, 5, '国产之光！战斗系统和画面都是顶级水平！', 1, 560),
(3, 9, 5, '天命人，踏上西行之路吧！', 1, 450);

-- 给测试用户添加一些购物车数据
INSERT INTO `cart_items` (`user_id`, `game_id`, `quantity`) VALUES
(2, 3, 1),
(2, 6, 1);

-- 给测试用户添加愿望单
INSERT INTO `wishlist` (`user_id`, `game_id`) VALUES
(2, 4),
(2, 9),
(3, 1),
(3, 10);

-- 给测试用户添加一些已购买的游戏
INSERT INTO `orders` (`order_no`, `user_id`, `total_amount`, `pay_amount`, `discount_amount`, `status`, `pay_time`) VALUES
('ORD202401150001', 2, 298.00, 149.00, 149.00, 'COMPLETED', '2024-01-15 10:30:00'),
('ORD202401200002', 2, 0.00, 0.00, 0.00, 'COMPLETED', '2024-01-20 14:20:00');

INSERT INTO `order_items` (`order_id`, `game_id`, `game_title`, `game_cover`, `price`, `quantity`) VALUES
(1, 1, '赛博朋克 2077', '/game-assets/cyberpunk-2077/cover.jpg', 149.00, 1),
(2, 5, 'CS2 反恐精英2', '/game-assets/counter-strike-2/cover.jpg', 0.00, 1);

INSERT INTO `user_library` (`user_id`, `game_id`, `order_id`, `play_time`, `last_played_at`) VALUES
(2, 1, 1, 1250, '2024-01-28 22:15:00'),
(2, 5, 2, 3680, '2024-01-29 23:45:00');

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `flash_sales` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `game_id` BIGINT NOT NULL COMMENT '游戏ID',
    `flash_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    `stock_count` INT NOT NULL COMMENT '秒杀活动库存数量',
    `sold_count` INT DEFAULT 0 COMMENT '已售出数量',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `per_user_limit` INT DEFAULT 1 COMMENT '每人限购数量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
    INDEX `idx_game_id` (`game_id`),
    INDEX `idx_start_end` (`start_time`, `end_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- 秒杀订单记录表（用于校验每人限购）
CREATE TABLE IF NOT EXISTS `flash_sale_orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `flash_sale_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `game_id` BIGINT NOT NULL COMMENT '游戏ID',
    `order_id` BIGINT COMMENT '关联普通订单ID',
    `quantity` INT DEFAULT 1 COMMENT '购买数量',
    `price` DECIMAL(10,2) NOT NULL COMMENT '秒杀成交价格',
    `status` ENUM('PENDING', 'PAID', 'CANCELLED') DEFAULT 'PENDING' COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`flash_sale_id`) REFERENCES `flash_sales`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
    INDEX `idx_flash_user` (`flash_sale_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单记录表';

-- 初始化秒杀活动数据
-- 注：开始/结束时间使用相对于当前日期的动态值，实际运行时请根据需要调整
INSERT INTO `flash_sales` (`game_id`, `flash_price`, `stock_count`, `sold_count`, `start_time`, `end_time`, `per_user_limit`, `status`) VALUES
-- 进行中的秒杀：开始时间设为当前日期前1天，结束时间设为当前日期后2天
(1, 99.00, 100, 35, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 1, 1),
-- 即将开始的秒杀：开始时间设为当前日期后1小时
(2, 168.00, 50, 0, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 DAY), 1, 1),
-- 已结束的秒杀：开始/结束时间都在过去
(6, 88.00, 200, 200, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1, 1),
-- 已售罄的秒杀（仍在进行中但库存已空）
(10, 69.00, 10, 10, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 1, 1);

-- ===================== 积分体系表 =====================

-- 用户积分账户表
CREATE TABLE IF NOT EXISTS `points` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `balance` INT DEFAULT 0 COMMENT '当前积分余额',
    `total_earned` INT DEFAULT 0 COMMENT '累计获得积分',
    `total_spent` INT DEFAULT 0 COMMENT '累计消耗积分',
    `consecutive_days` INT DEFAULT 0 COMMENT '连续签到天数',
    `last_sign_date` DATE COMMENT '最后签到日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分账户表';

-- 积分流水记录表
CREATE TABLE IF NOT EXISTS `point_logs` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` ENUM('EARN', 'SPEND') NOT NULL COMMENT '类型: EARN获得 SPEND消耗',
    `amount` INT NOT NULL COMMENT '积分数量',
    `balance_after` INT NOT NULL COMMENT '变动后余额',
    `source` VARCHAR(50) NOT NULL COMMENT '来源/用途: SIGN_IN消费 REVIEW评论 EXCHANGE兑换等',
    `source_id` VARCHAR(100) COMMENT '关联ID(如订单号、商品ID)',
    `description` VARCHAR(200) COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水记录表';

-- 积分商品表
CREATE TABLE IF NOT EXISTS `point_products` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` VARCHAR(500) COMMENT '商品描述',
    `image` VARCHAR(500) COMMENT '商品图片',
    `points_required` INT NOT NULL COMMENT '所需积分',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `sold_count` INT DEFAULT 0 COMMENT '已兑换数量',
    `type` ENUM('COUPON', 'VIRTUAL') DEFAULT 'VIRTUAL' COMMENT '类型: COUPON优惠券 VIRTUAL虚拟物品',
    `value` DECIMAL(10,2) COMMENT '面值(优惠券时使用)',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0下架 1上架',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_status` (`status`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商品表';

-- 兑换记录表
CREATE TABLE IF NOT EXISTS `exchange_records` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称(冗余)',
    `product_image` VARCHAR(500) COMMENT '商品图片(冗余)',
    `points_spent` INT NOT NULL COMMENT '消耗积分',
    `status` ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'COMPLETED' COMMENT '状态',
    `redeem_code` VARCHAR(100) COMMENT '兑换码(优惠券时使用)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `point_products`(`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录表';

-- 初始化积分商品数据
INSERT INTO `point_products` (`name`, `description`, `image`, `points_required`, `stock`, `type`, `value`, `sort_order`) VALUES
('10元优惠券', '全场通用优惠券，满100可用', '/game-assets/cyberpunk-2077/cover.jpg', 100, 999, 'COUPON', 10.00, 1),
('20元优惠券', '全场通用优惠券，满200可用', '/game-assets/elden-ring/cover.jpg', 180, 500, 'COUPON', 20.00, 2),
('50元优惠券', '全场通用优惠券，满500可用', '/game-assets/hogwarts-legacy/cover.jpg', 400, 200, 'COUPON', 50.00, 3),
('100元优惠券', '全场通用优惠券，满1000可用', '/game-assets/baldurs-gate-3/cover.jpg', 750, 100, 'COUPON', 100.00, 4),
('稀有头像框', '限定稀有头像框，彰显尊贵身份', '/game-assets/sekiro-shadows-die-twice/cover.jpg', 500, 999, 'VIRTUAL', NULL, 5),
('游戏主题皮肤', '独家定制游戏主题UI皮肤', '/game-assets/civilization-6/cover.jpg', 800, 500, 'VIRTUAL', NULL, 6),
('VIP会员7天', '享受专属VIP特权7天', '/game-assets/stardew-valley/cover.jpg', 300, 999, 'VIRTUAL', NULL, 7),
('限定徽章', '积分达人专属纪念徽章', '/game-assets/black-myth-wukong/cover.jpg', 1000, 100, 'VIRTUAL', NULL, 8);

-- 初始化用户积分账户
INSERT INTO `points` (`user_id`, `balance`, `total_earned`, `consecutive_days`) VALUES
(1, 500, 500, 3),
(2, 200, 200, 1),
(3, 0, 0, 0);

-- ===================== 充值模块表 =====================

-- 充值套餐表
CREATE TABLE IF NOT EXISTS `recharge_plans` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    `bonus_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '赠送金额',
    `description` VARCHAR(200) COMMENT '套餐描述',
    `icon` VARCHAR(200) COMMENT '套餐图标',
    `tag` VARCHAR(50) COMMENT '标签(如 首充特惠、限时优惠)',
    `tag_color` VARCHAR(20) COMMENT '标签颜色',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0下架 1上架',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_status` (`status`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值套餐表';

-- 充值订单表
CREATE TABLE IF NOT EXISTS `recharge_orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `plan_id` BIGINT COMMENT '套餐ID',
    `plan_name` VARCHAR(100) COMMENT '套餐名称(冗余)',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    `bonus_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '赠送金额',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '实际到账金额',
    `pay_method` VARCHAR(20) DEFAULT 'WECHAT' COMMENT '支付方式: WECHAT支付宝 ALIPAY微信',
    `status` ENUM('PENDING', 'PAID', 'CANCELLED', 'TIMEOUT') DEFAULT 'PENDING' COMMENT '订单状态: PENDING待支付 PAID已支付 CANCELLED已取消 TIMEOUT已超时',
    `pay_time` DATETIME COMMENT '支付时间',
    `expire_time` DATETIME COMMENT '过期时间',
    `transaction_id` VARCHAR(100) COMMENT '第三方交易号',
    `remark` VARCHAR(200) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    FOREIGN KEY (`plan_id`) REFERENCES `recharge_plans`(`id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值订单表';

-- 余额流水表
CREATE TABLE IF NOT EXISTS `balance_logs` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` ENUM('RECHARGE', 'CONSUME', 'REFUND') NOT NULL COMMENT '类型: RECHARGE充值 CONSUME消费 REFUND退款',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '变动金额(正数增加,负数减少)',
    `balance_after` DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
    `source` VARCHAR(50) NOT NULL COMMENT '来源: RECHARGE充值 ORDER消费 CANCEL退款等',
    `source_id` VARCHAR(100) COMMENT '关联ID(如订单号、充值单号)',
    `description` VARCHAR(200) COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='余额流水表';

-- 初始化充值套餐数据
INSERT INTO `recharge_plans` (`name`, `amount`, `bonus_amount`, `description`, `tag`, `tag_color`, `sort_order`) VALUES
('新手礼包', 10.00, 2.00, '首次充值专享，立得12元', '首充特惠', '#ef4444', 1),
('10元套餐', 10.00, 0.00, '充值10元，立即到账', NULL, NULL, 2),
('30元套餐', 30.00, 2.00, '充值30元，赠送2元', '热门推荐', '#f59e0b', 3),
('50元套餐', 50.00, 5.00, '充值50元，赠送5元', NULL, NULL, 4),
('100元套餐', 100.00, 12.00, '充值100元，赠送12元', '超值', '#10b981', 5),
('200元套餐', 200.00, 30.00, '充值200元，赠送30元', NULL, NULL, 6),
('500元套餐', 500.00, 88.00, '充值500元，赠送88元', 'VIP专享', '#8b5cf6', 7),
('1000元套餐', 1000.00, 200.00, '充值1000元，赠送200元', '钻石会员', '#ec4899', 8);
