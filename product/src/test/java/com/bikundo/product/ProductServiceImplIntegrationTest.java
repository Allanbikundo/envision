package com.bikundo.product;

import com.bikundo.product.dtos.CreateProductRequest;
import com.bikundo.product.dtos.ProductDto;
import com.bikundo.product.implementations.ProductServiceImpl;
import com.bikundo.product.models.Product;
import com.bikundo.product.repositories.ProductRepository;
import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductServiceImplIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductRepository productRepository;

    private final Long TEST_ID = 1L;
    private final String TEST_SKU = "SG-A55";
    private final String TEST_NAME = "Samsung Galaxy A55";
    private final BigDecimal TEST_PRICE = new BigDecimal("54000");
    private final Integer TEST_STOCK = 15;

    private CreateProductRequest testRequest;
    private Product testProduct;
    private ProductDto testDto;

    @BeforeEach
    void setUp() {
        testRequest = new CreateProductRequest();
        testRequest.setName(TEST_NAME);
        testRequest.setSku(TEST_SKU);
        testRequest.setBrand("Samsung");
        testRequest.setCategory("Smartphone");
        testRequest.setDescription("Great midrange phone");
        testRequest.setPrice(TEST_PRICE);
        testRequest.setStockQuantity(TEST_STOCK);

        testProduct = Product.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .sku(TEST_SKU)
                .brand("Samsung")
                .category("Smartphone")
                .description("Great midrange phone")
                .price(TEST_PRICE)
                .stockQuantity(TEST_STOCK)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testDto = new ProductDto();
        testDto.setId(TEST_ID);
        testDto.setName(TEST_NAME);
        testDto.setSku(TEST_SKU);
        testDto.setBrand("Samsung");
        testDto.setCategory("Smartphone");
        testDto.setDescription("Great midrange phone");
        testDto.setPrice(TEST_PRICE);
        testDto.setStockQuantity(TEST_STOCK);
        testDto.setCreatedAt(Instant.now());
        testDto.setUpdatedAt(Instant.now());
    }

    @Test
    void createProduct_shouldSaveAndReturnDto() {
        // When
        ProductDto actualDto = productService.createProduct(testRequest);

        // Then
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getId()).isNotNull();
        assertThat(actualDto.getName()).isEqualTo(TEST_NAME);
        assertThat(actualDto.getSku()).isEqualTo(TEST_SKU);
        assertThat(actualDto.getPrice()).isEqualTo(TEST_PRICE);
        assertThat(actualDto.getStockQuantity()).isEqualTo(TEST_STOCK);
    }

    @Test
    void getProductById_shouldReturnProduct() {
        // Given
        ProductDto createdDto = productService.createProduct(testRequest);

        // When
        ProductDto actualDto = productService.getProductById(createdDto.getId());

        // Then
        assertThat(actualDto).isNotNull();
        assertThat(actualDto).isEqualTo(createdDto);
    }

    @Test
    void getProductBySku_shouldReturnProduct() {
        // Given
        ProductDto createdDto = productService.createProduct(testRequest);

        // When
        ProductDto actualDto = productService.getProductBySku(TEST_SKU);

        // Then
        assertThat(actualDto).isNotNull();
        assertThat(actualDto).isEqualTo(createdDto);
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        // Given
        ProductDto createdDto = productService.createProduct(testRequest);

        // When
        List<ProductDto> actualDtos = productService.getAllProducts();

        // Then
        assertThat(actualDtos).isNotNull();
        assertThat(actualDtos).isNotEmpty();
        assertThat(actualDtos.get(0)).isEqualTo(createdDto);
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProduct() {
        // Given
        ProductDto createdDto = productService.createProduct(testRequest);
        CreateProductRequest updateRequest = new CreateProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setSku("UPDATED-SKU");
        updateRequest.setBrand("Updated Brand");
        updateRequest.setCategory("Updated Category");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("60000"));
        updateRequest.setStockQuantity(20);

        // When
        ProductDto actualDto = productService.updateProduct(createdDto.getId(), updateRequest);

        // Then
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getName()).isEqualTo(updateRequest.getName());
        assertThat(actualDto.getSku()).isEqualTo(updateRequest.getSku());
        assertThat(actualDto.getBrand()).isEqualTo(updateRequest.getBrand());
        assertThat(actualDto.getCategory()).isEqualTo(updateRequest.getCategory());
        assertThat(actualDto.getDescription()).isEqualTo(updateRequest.getDescription());
        assertThat(actualDto.getPrice()).isEqualTo(updateRequest.getPrice());
        assertThat(actualDto.getStockQuantity()).isEqualTo(updateRequest.getStockQuantity());
    }

    @Test
    void deleteProduct_shouldDeleteExistingProduct() {
        // Given
        ProductDto createdDto = productService.createProduct(testRequest);

        // When
        productService.deleteProduct(createdDto.getId());

        // Then
        assertThat(productService.getProductById(createdDto.getId())).isNull();
    }
}
