package com.dairy.user.service;

import com.dairy.common.exception.InvalidOperationException;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.user.dto.CreateAddressRequest;
import com.dairy.user.entity.Address;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AddressService addressService;

    private AppUser appUser;
    private Address address;
    private CreateAddressRequest createAddressRequest;

    @BeforeEach
    void setUp() {
        appUser = mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        address = new Address(appUser, "Line 1", "Line 2", "City", "123456", true);
        createAddressRequest = new CreateAddressRequest(1L, "Line 1", "Line 2", "City", "123456", true);
    }

    @Test
    void createAddress_Success() {
        when(userService.getUser(1L)).thenReturn(appUser);
        when(addressRepository.save(any())).thenReturn(address);

        Address result = addressService.createAddress(createAddressRequest);

        assertNotNull(result);
        assertTrue(result.isDefaultAddress());
        verify(addressRepository, times(1)).save(any());
    }

    @Test
    void getUserAddresses_Success() {
        List<Address> addresses = List.of(address);
        when(addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(1L)).thenReturn(addresses);

        List<Address> result = addressService.getUserAddresses(1L);

        assertEquals(1, result.size());
        assertEquals("City", result.get(0).getCity());
    }

    @Test
    void getAddressForUser_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Address result = addressService.getAddressForUser(1L, 1L);

        assertNotNull(result);
        assertEquals("City", result.getCity());
    }

    @Test
    void getAddressForUser_NotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressForUser(1L, 1L));
    }

    @Test
    void getAddressForUser_NotBelongToUser() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(InvalidOperationException.class, () -> addressService.getAddressForUser(2L, 1L));
    }

    @Test
    void getDefaultAddress_Success() {
        when(addressRepository.findByUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.of(address));

        Address result = addressService.getDefaultAddress(1L);

        assertNotNull(result);
        assertTrue(result.isDefaultAddress());
    }

    @Test
    void getDefaultAddress_NotFound() {
        when(addressRepository.findByUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.getDefaultAddress(1L));
    }

    @Test
    void markDefault_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        List<Address> userAddresses = new ArrayList<>();
        userAddresses.add(address);
        when(addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(1L)).thenReturn(userAddresses);

        Address result = addressService.markDefault(1L, 1L);

        assertNotNull(result);
        assertTrue(result.isDefaultAddress());
    }
}
