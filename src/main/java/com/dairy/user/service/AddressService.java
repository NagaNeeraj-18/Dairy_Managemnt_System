package com.dairy.user.service;

import com.dairy.user.dto.CreateAddressRequest;
import com.dairy.user.entity.Address;
import com.dairy.user.entity.AppUser;
import com.dairy.user.repository.AddressRepository;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.InvalidOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressService(AddressRepository addressRepository, UserService userService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    @Transactional
    public Address createAddress(CreateAddressRequest request) {
        log.info("Creating new address for user ID: {}", request.userId());
        AppUser user = userService.getUser(request.userId());
        boolean shouldBeDefault = request.defaultAddress()
                || addressRepository.findByUserIdAndDefaultAddressTrue(user.getId()).isEmpty();

        if (shouldBeDefault) {
            log.info("Address marked as default. Clearing other defaults for user ID: {}", user.getId());
            clearDefaults(user.getId());
        }

        Address savedAddress = addressRepository.save(new Address(
                user,
                request.line1(),
                request.line2(),
                request.city(),
                request.pincode(),
                shouldBeDefault
        ));
        log.info("Address created successfully with ID: {} for user ID: {}", savedAddress.getId(), user.getId());
        return savedAddress;
    }

    @Transactional(readOnly = true)
    public List<Address> getUserAddresses(Long userId) {
        log.debug("Fetching all addresses for user ID: {}", userId);
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(userId);
    }

    @Transactional(readOnly = true)
    public Address getAddressForUser(Long userId, Long addressId) {
        log.debug("Fetching address ID: {} for user ID: {}", addressId, userId);
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("Address not found with ID: {}", addressId);
                    return new ResourceNotFoundException("Address not found: " + addressId);
                });
        if (!address.getUser().getId().equals(userId)) {
            log.warn("Address ownership validation failed. Address ID: {} does not belong to user ID: {}", addressId, userId);
            throw new InvalidOperationException("Address does not belong to this user");
        }
        return address;
    }

    @Transactional(readOnly = true)
    public Address getDefaultAddress(Long userId) {
        log.debug("Fetching default address for user ID: {}", userId);
        return addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .orElseThrow(() -> {
                    log.warn("Default address not found for user ID: {}", userId);
                    return new ResourceNotFoundException("Default address not found for user: " + userId);
                });
    }

    @Transactional
    public Address markDefault(Long userId, Long addressId) {
        log.info("Marking address ID: {} as default for user ID: {}", addressId, userId);
        Address address = getAddressForUser(userId, addressId);
        clearDefaults(userId);
        address.markDefault();
        log.info("Address ID: {} successfully set as default for user ID: {}", addressId, userId);
        return address;
    }

    private void clearDefaults(Long userId) {
        log.debug("Clearing default flags on all addresses for user ID: {}", userId);
        addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(userId)
                .forEach(Address::clearDefault);
    }
}
