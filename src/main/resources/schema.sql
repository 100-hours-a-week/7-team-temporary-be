CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(50) NOT NULL,
  password VARCHAR(60) NOT NULL,
  nickname VARCHAR(10) NOT NULL,
  gender VARCHAR(20) NOT NULL,
  birth DATE NOT NULL,
  focus_time_zone VARCHAR(20) NOT NULL,
  day_end_time TIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_users_email_deleted_at (email, deleted_at),
  INDEX idx_users_nickname_deleted_at (nickname, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  image_type VARCHAR(20) NOT NULL,
  upload_key CHAR(36) NOT NULL,
  upload_status VARCHAR(20) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_image_status_deleted_expires (upload_status, deleted_at, expires_at),
  INDEX idx_image_upload_key_deleted (upload_key, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  image_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_user_image_user_deleted (user_id, deleted_at),
  CONSTRAINT fk_user_image_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_image_image_id FOREIGN KEY (image_id) REFERENCES image(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS friend_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  to_user_id BIGINT NOT NULL,
  from_user_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  responded_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_friend_request_to_status_deleted_created (to_user_id, status, deleted_at, created_at),
  CONSTRAINT fk_friend_request_to_user FOREIGN KEY (to_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS friend (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  friend_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_friend_user_deleted (user_id, deleted_at),
  CONSTRAINT fk_friend_user_id FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  title VARCHAR(30) NOT NULL,
  content VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL,
  scheduled_at DATETIME(6) NOT NULL,
  sent_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_notification_user_deleted_sent (user_id, deleted_at, sent_at),
  CONSTRAINT fk_notification_user_id FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_fcm_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  fcm_token VARCHAR(255) NOT NULL,
  platform VARCHAR(20) NOT NULL,
  is_active TINYINT(1) NOT NULL,
  last_seen_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_user_fcm_token_user_active_deleted (user_id, is_active, deleted_at),
  CONSTRAINT fk_user_fcm_token_user_id FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS day_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  plan_date DATE NOT NULL,
  ai_usage_remaining_count INT NOT NULL DEFAULT 2,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_day_plan_user_date_deleted (user_id, plan_date, deleted_at),
  CONSTRAINT fk_day_plan_user_id FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS schedule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  day_plan_id BIGINT NOT NULL,
  parent_schedule_id BIGINT NULL,
  title VARCHAR(25) NOT NULL,
  status VARCHAR(20) NOT NULL,
  type VARCHAR(20) NOT NULL,
  assigned_by VARCHAR(20) NOT NULL,
  assignment_status VARCHAR(20) NOT NULL,
  start_at TIME(6) NULL,
  end_at TIME(6) NULL,
  estimated_time_range VARCHAR(20) NULL,
  focus_level INTEGER NULL,
  is_urgent TINYINT(1) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  CONSTRAINT fk_schedule_day_plan_id FOREIGN KEY (day_plan_id) REFERENCES day_plan(id),
  CONSTRAINT fk_schedule_parent_id FOREIGN KEY (parent_schedule_id) REFERENCES schedule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS schedule_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  schedule_id BIGINT NOT NULL,
  event_type VARCHAR(20) NOT NULL,
  prev_start_at DATETIME(6) NULL,
  prev_end_at DATETIME(6) NULL,
  next_start_at DATETIME(6) NULL,
  next_end_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_schedule_history_schedule_deleted (schedule_id, deleted_at),
  CONSTRAINT fk_schedule_history_schedule_id FOREIGN KEY (schedule_id) REFERENCES schedule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS day_reflection (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  day_plan_id BIGINT NOT NULL,
  title CHAR(13) NOT NULL,
  content VARCHAR(200) NOT NULL,
  is_open TINYINT(1) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_day_reflection_user_deleted_created (user_id, deleted_at, created_at),
  INDEX idx_day_reflection_user_open_deleted_created (user_id, is_open, deleted_at, created_at),
  CONSTRAINT fk_day_reflection_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_day_reflection_day_plan_id FOREIGN KEY (day_plan_id) REFERENCES day_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS day_reflection_image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  day_reflection_id BIGINT NOT NULL,
  image_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_day_reflection_image_reflection_deleted (day_reflection_id, deleted_at),
  CONSTRAINT fk_day_reflection_image_reflection_id FOREIGN KEY (day_reflection_id) REFERENCES day_reflection(id),
  CONSTRAINT fk_day_reflection_image_image_id FOREIGN KEY (image_id) REFERENCES image(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reflection_like (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  day_reflection_id BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_reflection_like_user_reflection_deleted (user_id, day_reflection_id, deleted_at),
  CONSTRAINT fk_reflection_like_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_reflection_like_reflection_id FOREIGN KEY (day_reflection_id) REFERENCES day_reflection(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_room (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  title VARCHAR(25) NOT NULL,
  type VARCHAR(20) NOT NULL,
  description VARCHAR(125) NOT NULL,
  max_participants INTEGER NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_chat_room_title_deleted (title, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_room_participant (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  chat_room_id BIGINT NOT NULL,
  last_seen_message_id BIGINT NULL,
  camera_enabled TINYINT(1) NOT NULL,
  left_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_chat_room_participant_user_deleted (user_id, deleted_at),
  INDEX idx_chat_room_participant_room_deleted (chat_room_id, deleted_at),
  CONSTRAINT fk_chat_room_participant_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_chat_room_participant_room_id FOREIGN KEY (chat_room_id) REFERENCES chat_room(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  chat_room_id BIGINT NOT NULL,
  message_type VARCHAR(20) NOT NULL,
  content VARCHAR(3000) NULL,
  is_deleted TINYINT(1) NOT NULL,
  sent_at DATETIME(6) NOT NULL,
  sender_type VARCHAR(20) NOT NULL,
  sender_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_message_room_deleted_sent (chat_room_id, deleted_at, sent_at),
  CONSTRAINT fk_message_chat_room_id FOREIGN KEY (chat_room_id) REFERENCES chat_room(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message_image (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  message_id BIGINT NOT NULL,
  image_id BIGINT NOT NULL,
  sort_order INTEGER NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_message_image_message_deleted (message_id, deleted_at),
  CONSTRAINT fk_message_image_message_id FOREIGN KEY (message_id) REFERENCES message(id),
  CONSTRAINT fk_message_image_image_id FOREIGN KEY (image_id) REFERENCES image(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS terms (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  terms_type VARCHAR(20) NOT NULL,
  is_active TINYINT(1) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS terms_sign (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  terms_id BIGINT NOT NULL,
  is_agreed TINYINT(1) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_terms_sign_user_terms_agreed_deleted (user_id, terms_id, is_agreed, deleted_at),
  CONSTRAINT fk_terms_sign_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_terms_sign_terms_id FOREIGN KEY (terms_id) REFERENCES terms(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  ai_report_response_limit INTEGER NOT NULL,
  ai_report_response_used INTEGER NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_report_user_start_deleted (user_id, start_date, deleted_at),
  CONSTRAINT fk_report_user_id FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report_daily_stat (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT NOT NULL,
  report_date DATE NOT NULL,
  achievement_rate INTEGER NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_report_daily_stat_report_deleted (report_id, deleted_at),
  CONSTRAINT fk_report_daily_stat_report_id FOREIGN KEY (report_id) REFERENCES report(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS report_chat_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT NOT NULL,
  message_type VARCHAR(20) NOT NULL,
  content VARCHAR(3000) NULL,
  is_deleted TINYINT(1) NOT NULL,
  sent_at DATETIME(6) NOT NULL,
  sender_type VARCHAR(20) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  INDEX idx_report_chat_message_report_deleted_sent (report_id, deleted_at, sent_at),
  CONSTRAINT fk_report_chat_message_report_id FOREIGN KEY (report_id) REFERENCES report(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
