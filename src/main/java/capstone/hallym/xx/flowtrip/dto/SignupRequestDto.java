package capstone.hallym.xx.flowtrip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequestDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 4, message = "비밀번호는 4자 이상 입력해주세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}