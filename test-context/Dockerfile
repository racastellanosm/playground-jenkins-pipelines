FROM php:8.4-cli-alpine

COPY --from=mlocati/php-extension-installer:2.11.1 /usr/bin/install-php-extensions /usr/local/bin/
COPY --from=composer/composer:2.10-bin /composer /usr/bin/composer

# Install PHP extensions
RUN install-php-extensions pdo_mysql amqp opcache pcntl

# Set the working directory
WORKDIR /app

# Install Composer
ADD composer.json composer.lock /app/
RUN composer install --no-scripts

# Copy the rest of the application
COPY . /app