# Online Coding Learning Platform

# Overview

Online Coding Learning Platform is a web-based e-learning application focused on programming education, developed using Java Spring MVC and Thymeleaf.

The platform is designed to support the core workflow of an online coding course system, where instructors can create and manage coding lessons, and students can enroll in courses, study the lessons, and track their learning progress.

This project emphasizes server-side web development with Java, applying the MVC architecture to build a structured, maintainable, and extensible learning platform. It also serves as a foundation for future enhancements such as coding exercises, automated evaluation, and AI-assisted learning support.

# Core Features

# Instructor Features

Create and manage coding lessons

Organize lesson content for programming courses

# Student Features

Register and join coding courses

Access lesson materials

Track learning progress throughout the course

# System Architecture

MVC (Model – View – Controller) pattern

Clear separation between business logic, presentation, and data access

Server-side rendering using Thymeleaf

# Technology Stack

Java

Spring Boot (Spring MVC)

Thymeleaf

HTML, CSS, JavaScript

Microsoft SQL Server

# Project Goals

Practice Java web application development using Spring MVC

Apply MVC architecture in a real-world-inspired project

Build a functional coding education platform with clear user roles

Prepare a scalable base for future features such as:

Coding exercises and submissions

Automatic code evaluation

AI-assisted learning guidance

# Contributors

Hoang Vu

Le Minh Triet

Phan Van Huy

Nguyen Tran Viet An

Tran An Quoc

# Notes

This project is developed for learning, practice, and demonstration purposes, with a focus on building a coding-oriented learning platform rather than a general e-learning system.

## Installation & Run

### Prerequisites

- JDK 22+
- MySQL
- Maven

### Steps

```bash
git clone https://github.com/BimCode1919/Online-Learning-Platform.git
cd Online-Learning-Platform
mvn clean install
mvn spring-boot:run
```

## 🗄️ Database Setup

### Step 1: Create database

```sql
CREATE DATABASE OnlineLearningDB
```

### Step 2: Change username & passowrd

You need to change your SQL username and password in the application.properties file.
