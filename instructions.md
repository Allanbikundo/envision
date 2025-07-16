## 🧪 Local Testing Instructions

Follow these steps to run and test the full system locally:

---

### ✅ 1. Add Local Domain to Hosts File

Edit your `/etc/hosts` (Linux/macOS) or `C:\Windows\System32\drivers\etc\hosts` (Windows) and add the following entry:

```
127.0.0.1   envision.ambience.co.ke
```

---

### ✅ 2. Start All Services with Docker Compose

From the root of the project, run:

```bash
docker compose up -d
```

Wait until all services are fully started and healthy.

---

### ✅ 3. Access Keycloak Admin Panel

Visit:

```
http://envision.ambience.co.ke:8080
```

Login credentials:

- **Username:** `admin`
- **Password:** `admin` 

*(very strong, we know)*

You can use this panel to manage users, realms, roles, and clients.

---

### ✅ 4. Import the Realm Configuration

To use a predefined realm (e.g. `envision-realm`):

1. Visit `http://envision.ambience.co.ke:8080/admin`
2. Log in as `admin`
3. Go to the left sidebar → click **Realm Settings** → **Import** (or from the main dashboard, choose *Add Realm → Import*)
4. Upload the `ecommerce-realm.json` file (found in the  `./keycloak` folder)
5. Click **Create**

This will import clients, roles, and configurations used by the system.

---

### ✅ 5. Access the Gateway (Sign Up)

Visit:

```
http://envision.ambience.co.ke:9080
```

This is the public API gateway. You can **sign up** or **log in** from here.

---

### ✅ 6. Get an Access Token

After logging in, you will receive an **access token** (JWT). This token is automatically refreshed on login.

You will use this token to authorize API calls via Swagger.

---

### ✅ 7. Authorize Swagger UI

For both `order-service` and `product-service`, open their Swagger UIs and click **"Authorize"**:

- **Order Service Swagger:** `http://envision.ambience.co.ke:8082/swagger-ui.html`
- **Product Service Swagger:** `http://envision.ambience.co.ke:8081/swagger-ui.html`

Paste your **Bearer token** (`eyJ...`) into the input field. This allows you to call secured endpoints.

---

### ✅ 8. Add a Product

- Go to the **Product Service Swagger UI**
- Use the `POST /api/products` endpoint to create a new product
- Example payload:

```json
{
  "name": "Samsung Galaxy A55",
  "sku": "SG-A55",
  "brand": "Samsung",
  "category": "Smartphone",
  "description": "Great midrange phone",
  "price": 54000,
  "stockQuantity": 10
}
```

---

### ✅ 9. Add an Address

- Go to the **Order Service Swagger UI**
- Use the `POST /api/addresses` endpoint to add a shipping/billing address

Example payload:

```json
{
  "addressLine1": "123 Main St",
  "city": "Nairobi",
  "country": "Kenya",
  "addressType": "BOTH"
}
```

---

### ✅ 10. Place an Order

- Use the `POST /api/orders` endpoint in **Order Service Swagger**
- Provide the `shippingAddressId`, `billingAddressId`, and `productId` from previous steps

Example payload:

```json
{
  "shippingAddressId": 1,
  "billingAddressId": 1,
  "items": [
    {
      "productId": 1,
      "productSku": "SG-A55",
      "quantity": 2
    }
  ],
  "externalReference": "WEB-ORDER-001",
  "contactEmail": "user@example.com",
  "contactPhone": "0722000000"
}
```

---

## ✅ You're Done!

If all goes well:
- Your order will be created
- Inventory will be validated
- Events will be passed through RabbitMQ
- The system will respond with a confirmed or failed order status

---