package com.family.spend.controller;

import com.family.spend.model.Member;
import com.family.spend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private MemberRepository memberRepository;

    // Returns public member profiles (exclusing passcodes)
    @GetMapping("/members")
    public ResponseEntity<List<Map<String, Object>>> getPublicMembers() {
        List<Member> members = memberRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Member m : members) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("role", m.getRole());
            map.put("avatarColor", m.getAvatarColor());
            map.put("messengerLink", m.getMessengerLink());
            map.put("messengerId", m.getMessengerId());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        String memberId = payload.get("memberId");
        String passcode = payload.get("passcode");

        Map<String, Object> response = new HashMap<>();
        if (memberId == null || passcode == null) {
            response.put("success", false);
            response.put("error", "Thiếu mã thành viên hoặc mật khẩu.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isPresent()) {
            Member m = memberOpt.get();
            if (passcode.equals(m.getPasscode())) {
                response.put("success", true);
                
                Map<String, Object> memberInfo = new HashMap<>();
                memberInfo.put("id", m.getId());
                memberInfo.put("name", m.getName());
                memberInfo.put("role", m.getRole());
                memberInfo.put("avatarColor", m.getAvatarColor());
                memberInfo.put("messengerLink", m.getMessengerLink());
                memberInfo.put("messengerId", m.getMessengerId());
                
                response.put("member", memberInfo);
                return ResponseEntity.ok(response);
            }
        }

        response.put("success", false);
        response.put("error", "Mật khẩu gia đình chưa chính xác. Vui lòng kiểm tra lại!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-passcode")
    public ResponseEntity<Map<String, Object>> changePasscode(@RequestBody Map<String, String> payload) {
        String memberId = payload.get("memberId");
        String oldPasscode = payload.get("oldPasscode");
        String newPasscode = payload.get("newPasscode");

        Map<String, Object> response = new HashMap<>();
        if (memberId == null || oldPasscode == null || newPasscode == null) {
            response.put("success", false);
            response.put("error", "Thông tin nhập không đầy đủ.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isPresent()) {
            Member m = memberOpt.get();
            if (oldPasscode.equals(m.getPasscode())) {
                m.setPasscode(newPasscode);
                memberRepository.save(m);
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
        }

        response.put("success", false);
        response.put("error", "Mật khẩu cũ không đúng.");
        return ResponseEntity.ok(response);
    }
}
