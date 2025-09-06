package com.example.demo.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.HashUtil;
import com.example.demo.Dto.AdminUserDto;
import com.example.demo.model.AdminUserBean;
import com.example.demo.repository.AdminUserRepository;

@RestController
@RequestMapping("/api/v1")
public class AdminController {
    

    @Autowired
    private AdminUserRepository adminRepository;

   @PostMapping("/login")
public Map<String, String> login(@RequestBody AdminUserDto loginRequest) {
    Map<String, String> response = new HashMap<>();
    AdminUserBean user = adminRepository.findByUserName(loginRequest.getUserName());

    if (user == null) {
        response.put("iserror", "true");
        response.put("message", "Invalid username or password please check");
        return response;
    }

    // Hash input password
    String hashedInputPassword = HashUtil.md5(loginRequest.getPassword());

    if (!user.getPassword().equalsIgnoreCase(hashedInputPassword)) {
        response.put("iserror", "true");
        response.put("message", "Invalid username or password");
    } else {
        response.put("iserror", "false");
        response.put("message", "Login successful");
    }

    return response;
}


}
