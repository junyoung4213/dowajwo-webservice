package kr.dowajwo.springboot.config.auth;

import kr.dowajwo.springboot.config.auth.dto.OAuthAttributes;
import kr.dowajwo.springboot.config.auth.dto.SessionUser;
import kr.dowajwo.springboot.domain.user.User;
import kr.dowajwo.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 현재 로그인 진행 중인 서비스를 구분하는 코드
        String userNameAttributeName = userRequest // OAuth2 로그인 진행시 키가 되는 필드값. Primary Key와 같은 의미.
                .getClientRegistration().getProviderDetails() // 구글의 경우 기본적으로 코드를 지원하지만, 네이버 카카오 등은 기본 지원하지 않음.
                .getUserInfoEndpoint().getUserNameAttributeName(); // 이후 네이버 로그인과 구글 로그인을 동시 지원할 때 사용됨.

        OAuthAttributes attributes = OAuthAttributes // OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스
                .of(registrationId, userNameAttributeName,oAuth2User.getAttributes()); // 이후 네이버 등 다른 소셜 로그인도 이 클래스 사용.

        User user = saveOrUpdate(attributes);

        httpSession.setAttribute("user",new SessionUser(user)); // 세션에 사용자 정보를 저장하기 위한 Dto 클래스

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}
