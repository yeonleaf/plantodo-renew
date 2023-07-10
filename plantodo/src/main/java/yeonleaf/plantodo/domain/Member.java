package yeonleaf.plantodo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.dto.MemberReqDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    public Member(MemberReqDto memberReqDto) {
        this.email = memberReqDto.getEmail();
        this.password = memberReqDto.getPassword();
    }
}
