package com.bikundo.product;

import com.bikundo.product.dtos.CreateProductRequest;
import com.bikundo.product.dtos.ProductDto;
import com.bikundo.product.implementations.ProductServiceImpl;
import com.bikundo.product.mappers.ProductMapper;
import com.bikundo.product.models.Product;
import com.bikundo.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

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
        // Given
        when(productMapper.toEntity(testRequest)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testDto);

        // When
        ProductDto actualDto = productService.createProduct(testRequest);

        // Then
        assertNotNull(actualDto);
        assertEquals(testDto, actualDto);
        verify(productRepository).save(testProduct);
    }

    @Test
    void createProduct_shouldThrowExceptionForNullRequest() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(null);
        });
    }

    @Test
    void getProductById_shouldReturnProduct() {
        // Given
        when(productRepository.findById(TEST_ID)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testDto);

        // When
        ProductDto actualDto = productService.getProductById(TEST_ID);

        // Then
        assertNotNull(actualDto);
        assertEquals(testDto, actualDto);
        verify(productRepository).findById(TEST_ID);
    }

    @Test
    void getProductById_shouldThrowExceptionForNonExistingId() {
        // Given
        when(productRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductById(TEST_ID);
        });
    }

    @Test
    void getProductBySku_shouldReturnProduct() {
        // Given
        when(productRepository.findBySku(TEST_SKU)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testDto);

        // When
        ProductDto actualDto = productService.getProductBySku(TEST_SKU);

        // Then
        assertNotNull(actualDto);
        assertEquals(testDto, actualDto);
        verify(productRepository).findBySku(TEST_SKU);
    }

    @Test
    void getProductBySku_shouldThrowExceptionForNonExistingSku() {
        // Given
        when(productRepository.findBySku(TEST_SKU)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductBySku(TEST_SKU);
        });
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        // Given
        List<Product> products = List.of(testProduct);
        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testDto);

        // When
        List<ProductDto> actualDtos = productService.getAllProducts();

        // Then
        assertNotNull(actualDtos);
        assertEquals(1, actualDtos.size());
        assertEquals(testDto, actualDtos.get(0));
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProduct() {
        // Given
        when(productRepository.findById(TEST_ID)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(testDto);

        // When
        ProductDto actualDto = productService.updateProduct(TEST_ID, testRequest);

        // Then
        assertNotNull(actualDto);
        assertEquals(testDto, actualDto);
        verify(productRepository).findById(TEST_ID);
        verify(productRepository).save(productCaptor.capture());
        verify(productMapper).toDto(productCaptor.capture());
        
        Product updatedProduct = productCaptor.getValue();
        assertEquals(testRequest.getName(), updatedProduct.getName());
        assertEquals(testRequest.getSku(), updatedProduct.getSku());
        assertEquals(testRequest.getBrand(), updatedProduct.getBrand());
        assertEquals(testRequest.getCategory(), updatedProduct.getCategory());
        assertEquals(testRequest.getDescription(), updatedProduct.getDescription());
        assertEquals(testRequest.getPrice(), updatedProduct.getPrice());
        assertEquals(testRequest.getStockQuantity(), updatedProduct.getStockQuantity());
    }

    @Test
    void updateProduct_shouldThrowExceptionForNonExistingId() {
        // Given
        when(productRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(TEST_ID, testRequest);
        });
    }

    @Test
    void deleteProduct_shouldDeleteExistingProduct() {
        // Given
        when(productRepository.existsById(TEST_ID)).thenReturn(true);

        // When
        productService.deleteProduct(TEST_ID);

        // Then
        verify(productRepository).existsById(TEST_ID);
        verify(productRepository).deleteById(TEST_ID);
    }

    @Test
    void deleteProduct_shouldThrowExceptionForNonExistingId() {
        // Given
        when(productRepository.existsById(TEST_ID)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProduct(TEST_ID);
        });
    }
}
