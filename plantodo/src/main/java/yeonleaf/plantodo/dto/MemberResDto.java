package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.domain.Member;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResDto {

    private Long id;
    private String email;
    private String password;

    public MemberResDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.password = member.getPassword();
    }

}
