package com.helloworldweb.helloworld_guestbook.service;

import com.helloworldweb.helloworld_guestbook.domain.GuestBook;
import com.helloworldweb.helloworld_guestbook.domain.GuestBookComment;
import com.helloworldweb.helloworld_guestbook.domain.User;
import com.helloworldweb.helloworld_guestbook.dto.GuestBookCommentDto;
import com.helloworldweb.helloworld_guestbook.dto.GuestBookDto;
import com.helloworldweb.helloworld_guestbook.repository.GuestBookCommentRepository;
import com.helloworldweb.helloworld_guestbook.repository.GuestBookRepository;
import com.helloworldweb.helloworld_guestbook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GuestBookServiceImpl implements GuestBookService{

    private final UserRepository userRepository;
    private final GuestBookCommentRepository guestBookCommentRepository;

    @Override
    @Transactional
    public GuestBookDto addGuestBookComment(Long userId, GuestBookCommentDto guestBookCommentDto) {
        String callerEmail = getCallerEmailFromSecurityContextHolder();
        User caller = getUserByEmail(callerEmail);

        User owner = getUserWithGuestBookWithGuestBookCommentById(userId);
        GuestBook guestBook = owner.getGuestBook();
        GuestBookComment guestBookComment = guestBookCommentDto.toEntity();
        //연관관계 등록, GuestBook -> GuestBookComment CascadeType.PERSIST 이므로, 영속화된 GuestBook객체의 변화 -> persist의 전이 => GuestBookComment 의 save가 필요없음.
        guestBookComment.updateGuestBook(guestBook);
        guestBookComment.updateUser(caller);

        //ID를 얻기 위해 GuestBookComment를 저장. 연관관계 + cascade를 통해 등록하면, 트랜잭션 유지 기간중에는 아이디를 얻을 수 없음.(Transient 객체를 반환)
        guestBookCommentRepository.save(guestBookComment);

        return new GuestBookDto(guestBook,guestBook.getGuestBookComments());
    }

    @Override
    @Transactional(readOnly = true)
    public GuestBookDto getGuestBook(Long userId) {
        GuestBook guestBook = getUserWithGuestBookWithGuestBookCommentById(userId).getGuestBook();
        GuestBookDto guestBookDto =  new GuestBookDto(guestBook,guestBook.getGuestBookComments());
        return guestBookDto;
    }

    @Override
    @Transactional
    public GuestBookCommentDto updateGuestBookComment(GuestBookCommentDto guestBookCommentDto) {
        String callerEmail = getCallerEmailFromSecurityContextHolder();
        GuestBookComment guestBookComment = getGuestBookCommentWithUserById(guestBookCommentDto.getId());
        if (validateCaller(guestBookComment.getUser().getEmail(),callerEmail)) {
            return new GuestBookCommentDto(guestBookComment.updateGuestBookComment(guestBookCommentDto));
        }else{
            throw new IllegalCallerException("방명록 작성자만 수정할 수 있습니다.");
        }
    }

    @Override
    @Transactional
    public void deleteGuestBookComment(Long guestBookCommentId) {
        String callerEmail = getCallerEmailFromSecurityContextHolder();
        GuestBookComment guestBookComment = getGuestBookCommentWithUserById(guestBookCommentId);
        if(validateCaller(guestBookComment.getUser().getEmail(),callerEmail)) {
            guestBookCommentRepository.delete(guestBookComment);
        }else{
            throw new IllegalCallerException("방명록 작성자만 삭제할 수 있습니다.");

        }
    }

    private User getUserByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(()->new NoSuchElementException("해당 유저가 존재하지 않습니다."));
        return user;
    }

    private GuestBookComment getGuestBookCommentWithUserById(Long guestBookCommendId){
        return guestBookCommentRepository.findGuestBookCommentWithUserById(guestBookCommendId).orElseThrow(()-> new NoSuchElementException("해당 방명록이 존재하지 않습니다."));
    }

    private boolean validateCaller(String email, String callerEmail){
        if(email.equals(callerEmail)){
            return true;
        }else{
            return false;
        }
    }

    private User getUserWithGuestBookWithGuestBookCommentById(Long userId){
        User user = userRepository.findUserWithGuestBookWithGuestBookCommentsbyId(userId).orElseThrow
                (()-> new NoSuchElementException("해당 유저가 존재하지 않습니다."));
        return user;
    }

    private User getUserWithGuestBookById(Long userId){
        User user = userRepository.findUserWithGuestBookById(userId).orElseThrow(()-> new NoSuchElementException("해당 유저가 존재하지 않습니다."));
        return user;
    }

    private String getCallerEmailFromSecurityContextHolder(){
        User caller = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return caller.getEmail();
    }
}
