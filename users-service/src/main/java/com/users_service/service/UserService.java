package com.users_service.service;

import com.users_service.dto.*;
import com.users_service.openfeign.CompanyClient;
import com.users_service.repository.User;
import com.users_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CompanyClient companyClient;


    public UserService(UserRepository userRepository, ModelMapper modelMapper, CompanyClient companyClient) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.companyClient = companyClient;
    }

    public UserListDTO getAllUsers() {
        List<User> user = userRepository.findAll();

        List<UserDTO> userDTOList = user.stream()
                .map(u -> modelMapper.map(u, UserDTO.class))
                .collect(Collectors.toList());

        UserListDTO userListDTO = new UserListDTO();
        userListDTO.setUsers(userDTOList);
        return userListDTO;
    }
    public void deleteUsersByCompanyId(UUID id) {
        userRepository.deleteAllByCompanyId(id);
    }



    public void deleteUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateUser(UUID id, String firstName, String lastName, String phone) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = optionalUser.get();

        boolean isUpdated = false;

        if (firstName != null && !firstName.equals(user.getFirst_name())) {
            user.setFirst_name(firstName);
            isUpdated = true;
        }

        if (lastName != null && !lastName.equals(user.getLast_name())) {
            user.setLast_name(lastName);
            isUpdated = true;
        }

        if (phone != null && !phone.equals(user.getPhone_number())) {
            user.setPhone_number(phone);
            isUpdated = true;
        }

        if (!isUpdated) {
            throw new ResponseStatusException(HttpStatus.NOT_MODIFIED, "No changes detected");
        }

        return user;
    }


    public List<User> getUsersByCompanyId (UUID id) {
        List<User> users = userRepository.findAllByCompanyId(id);
        if (users.isEmpty()) {
            log.info("No users found for companyId (getUsersByCompanyId): {}", id);
        }
        return users;
    }

    public UUID getUserCompanyId(UUID id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            log.warn("No user found for company id: {}", id);
            return null;
        }
        User user = optionalUser.get();
        return user.getCompany_id();
    }

    public UserDTO getUserById (UUID id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            log.warn("No user found for user id: {}", id);
            return null;
        }
        User user = optionalUser.get();
        return modelMapper.map(user, UserDTO.class);
    }

    public User createUser(User user) {
        Optional<User> optionalUserDTO = userRepository.findByNameAndSurname(user.getFirst_name(), user.getLast_name());
        if (optionalUserDTO.isPresent()) {
            log.warn("User {} {} already exists", user.getFirst_name(), user.getLast_name());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        boolean companyExist = companyClient.companyExists(user.getCompany_id());
        if (!companyExist) {
            log.warn("User creation failed, company id {} not found", user.getCompany_id());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found with ID: " + user.getCompany_id());

        }
        return userRepository.save(user);
    }

    public UserResponseDTO getUsersOneCompany(UUID id) {
        List<User> users = getUsersByCompanyId(id);

        if (users == null) {
            users = Collections.emptyList();
        }
        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setCompanyId(id);
        responseDTO.setUsers(userDTOs);

        return responseDTO;
    }


    public List<UserWithCompanyDTO> getAllUsersAndCompany () {
        CompanyListDTO companyListDTO = companyClient.getAllCompanies();
        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
        List<UserWithCompanyDTO> result = new ArrayList<>();

        for (UserDTO userDTO : userDTOs) {
            CompanyDTO company = findCompanyById(userDTO.getCompany_id(), companyListDTO);
            result.add(new UserWithCompanyDTO(userDTO, company));
        }

        return result;
    }

    private CompanyDTO findCompanyById(UUID companyId, CompanyListDTO companyListDTO) {
        return companyListDTO.getCompanies().stream()
                .filter(company -> company.getId().equals(companyId))
                .findFirst()
                .orElse(null);
    }

    public UserAndCompanyDTO getUserAndCompany (UUID id) {
        UUID companyId = getUserCompanyId(id);
        UserDTO user = getUserById(id);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        CompanyDTO company = companyClient.getOneCompany(companyId);
        return new UserAndCompanyDTO(user, company);
    }
}
