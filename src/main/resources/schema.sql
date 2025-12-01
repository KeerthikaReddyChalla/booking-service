CREATE TABLE IF NOT EXISTS booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pnr VARCHAR(50) NOT NULL,
    flight_id VARCHAR(50) NOT NULL,
    
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,

    booking_date DATETIME,
    journey_date DATETIME,

    number_of_seats INT NOT NULL,
    total_price DOUBLE NOT NULL,

    cancelled BOOLEAN DEFAULT FALSE
);


CREATE TABLE IF NOT EXISTS passenger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    age INT NOT NULL,
    seat_number VARCHAR(10),
    meal_type VARCHAR(50),

    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
);
