package kr.dowajwo.springboot.config.auth;

import kr.dowajwo.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@EnableWebSecurity // Spring Security 설정들을 활성화
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable()// h2-console 화면을 사용하기 위해 해당 옵션 disable
                .and()
                    .authorizeRequests()// URL별 권한 관리를 설정하는 옵션의 시작점. 이게 선언되어야만 antMatchers 옵션 사용가능
                    .antMatchers("/","/css/**","/images/**","/js/**","/h2-console/**").permitAll() // 권한 관리 대상을 지정하는 옵션(permitAll로 전체 열람 권한줌)
                    .antMatchers("/api/v1/**").hasRole(Role.USER.name())// USER권한을 가진 사람만 접근 가능하게 설정
                    .anyRequest().authenticated()// 설정된 값들 이외 나머지 URL들. authenticated를 추가해서 나머지 URL은 인증된 사용자들에게만 허용가능하게 함
                .and()
                    .logout()
                        .logoutSuccessUrl("/") // 로그아웃 기능에 대한 여러 설정의 진입점. 로그아웃 성공시 / 주소로 이동
                .and()
                    .oauth2Login() // 로그인 기능에 대한 여러 설정의 진입점
                        .userInfoEndpoint()// 로그인 성공 이후 사용자 정보를 가져올 때의 설정들 담당
                            .userService(customOAuth2UserService); // 소셜 로그인 성공시 후속 조치 진행할 인터페이스 구현체 등록.
                                                                    // 리소스 서버(소셜 서비스)에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능 명시가능
    }
}
