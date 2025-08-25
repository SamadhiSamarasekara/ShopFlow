# Shop Management System

A comprehensive Java + JavaFX application for managing shop operations including products, categories, customers, orders, and payments.

## Features

### 🏷️ Category Management
- Create, edit, and delete product categories
- Active/inactive status management
- Category-based product organization

### 📦 Product Management
- Complete product information management
- Stock quantity tracking
- Low stock alerts
- SKU-based product identification
- Category association
- Price and cost management

### 👥 Customer Management
- Customer registration and profile management
- Contact information storage
- Address management
- Customer order history

### 🛒 Order Management
- Create and manage customer orders
- Order status tracking (Pending, Confirmed, Processing, Shipped, Delivered, Cancelled, Refunded)
- Order item management
- Automatic total calculations
- Tax and discount handling

### 💳 Payment Management
- Multiple payment methods (Cash, Credit Card, Debit Card, Bank Transfer, PayPal, etc.)
- Payment status tracking
- Transaction reference management
- Refund processing

### 📊 Dashboard
- Overview of key metrics
- Total products, customers, orders, and revenue
- Quick access to all modules

## Technology Stack

- **Java 17** - Core programming language
- **JavaFX 19** - Desktop GUI framework
- **Maven** - Build tool and dependency management
- **H2 Database** - Embedded database for data storage
- **FXML** - UI layout definition

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── com/shop/
│   │   │   ├── App.java                    # Main application class
│   │   │   ├── controller/                 # JavaFX controllers
│   │   │   │   ├── MainController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── PaymentController.java
│   │   │   ├── model/                      # Data models
│   │   │   │   ├── Category.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── Payment.java
│   │   │   ├── dao/                        # Data Access Objects
│   │   │   │   ├── CategoryDAO.java
│   │   │   │   ├── ProductDAO.java
│   │   │   │   ├── CustomerDAO.java
│   │   │   │   ├── OrderDAO.java
│   │   │   │   └── PaymentDAO.java
│   │   │   └── database/                   # Database configuration
│   │   │       └── DatabaseConfig.java
│   │   └── module-info.java               # Java module definition
│   └── resources/
│       └── fxml/                           # FXML view files
│           ├── MainView.fxml
│           ├── DashboardView.fxml
│           ├── CategoryView.fxml
│           ├── ProductView.fxml
│           ├── CustomerView.fxml
│           ├── OrderView.fxml
│           └── PaymentView.fxml
├── pom.xml                                 # Maven configuration
├── build.bat                               # Windows build script
└── run.bat                                 # Windows run script
```

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or later
- Apache Maven 3.6 or later
- Windows (for the provided batch scripts)

### Installation

1. **Clone or download the project** to your local machine

2. **Navigate to the project directory**:
   ```cmd
   cd "c:\Users\User\Desktop\java project"
   ```

3. **Build the project**:
   ```cmd
   build.bat
   ```

4. **Run the application**:
   ```cmd
   run.bat
   ```

### Alternative Maven Commands

If you prefer using Maven directly:

- **Clean and compile**:
  ```cmd
  mvn clean compile
  ```

- **Run the application**:
  ```cmd
  mvn javafx:run
  ```

- **Package the application**:
  ```cmd
  mvn clean package
  ```

## Database

The application uses an embedded H2 database that will be automatically created in the `database/` folder when you first run the application. The database includes the following tables:

- `categories` - Product categories
- `customers` - Customer information
- `products` - Product inventory
- `orders` - Customer orders
- `order_items` - Individual items within orders
- `payments` - Payment transactions

## Usage

### Starting the Application

1. Run the application using `run.bat` or `mvn javafx:run`
2. The main window will open with a dashboard showing key metrics
3. Use the sidebar navigation to access different modules

### Managing Categories

1. Click on "🏷️ Categories" in the sidebar
2. Fill in the category information form
3. Click "Save" to create a new category
4. Select a category from the table to edit or delete it

### Managing Products

1. Click on "📦 Products" in the sidebar
2. Fill in the product information form
3. Select a category from the dropdown
4. Set pricing and stock information
5. Click "Save" to create a new product

### Managing Customers

1. Click on "👥 Customers" in the sidebar
2. Enter customer details including contact information
3. Add address details for shipping
4. Click "Save" to register the customer

### Creating Orders

1. Click on "🛒 Orders" in the sidebar
2. Select a customer
3. Add products to the order
4. Set quantities and verify pricing
5. Apply any discounts or taxes
6. Save the order

### Processing Payments

1. Click on "💳 Payments" in the sidebar
2. Select an order to process payment for
3. Choose payment method
4. Enter payment details
5. Mark payment as completed

## Development

### Adding New Features

1. **Models**: Add new entity classes in `com.shop.model`
2. **DAOs**: Create corresponding DAO classes in `com.shop.dao`
3. **Controllers**: Implement JavaFX controllers in `com.shop.controller`
4. **Views**: Design FXML layouts in `src/main/resources/fxml`
5. **Database**: Update `DatabaseConfig.java` to include new tables

### Database Schema Changes

When modifying the database schema:

1. Update the table creation SQL in `DatabaseConfig.java`
2. Delete the existing `database/` folder to recreate the database
3. Update corresponding model and DAO classes

### Testing

The project structure supports JUnit testing. Add test classes in `src/test/java/` following the same package structure.

## Troubleshooting

### Common Issues

1. **JavaFX Runtime Error**: Ensure JavaFX modules are properly configured in your IDE
2. **Database Connection Error**: Check if the `database/` folder has write permissions
3. **Maven Build Error**: Verify Java 17+ and Maven 3.6+ are installed

### Logs and Debugging

- Application logs are displayed in the console
- Database files are stored in the `database/` folder
- Enable H2 Console by modifying the database URL in `DatabaseConfig.java`

## License

This project is provided as an educational example for Java + JavaFX development.

## Contributing

Feel free to fork this project and submit pull requests for improvements or bug fixes.
