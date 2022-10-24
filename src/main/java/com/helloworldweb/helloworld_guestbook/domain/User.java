package com.helloworldweb.helloworld_guestbook.domain;

import com.helloworldweb.helloworld_guestbook.dto.UserDto;
import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class User {

    @Id @GeneratedValue
    private Long id;

    @NotNull
    private String email;
    private String profileUrl;
    private String nickName;
    private String repoUrl;
    private String profileMusicName;
    private String profileMusicUrl;
    private String fcm;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BlogPost> blogPosts = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_book_id")
    private GuestBook guestBook;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GuestBookComment> guestBookComments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostSubComment> postSubComments = new ArrayList<>();

    @Builder
    public User (Long id, String email, String profileUrl, String nickName,String repoUrl, String profileMusicName, String profileMusicUrl, String fcm,
                 List<BlogPost> blogPosts, GuestBook guestBook, List<GuestBookComment> guestBookComments, List<PostSubComment> postSubComments){
        this.id = id;
        this.email = email;
        this.profileUrl = profileUrl;
        this.nickName = nickName;
        this.repoUrl = repoUrl;
        this.profileMusicName = profileMusicName;
        this.profileMusicUrl = profileMusicUrl;
        this.fcm = fcm;
        this.blogPosts = blogPosts == null ? new ArrayList<>() : blogPosts;
        this.guestBook = guestBook;
        this.guestBookComments = guestBookComments == null ? new ArrayList<>() : guestBookComments;
        this.postSubComments = postSubComments == null ? new ArrayList<>() : postSubComments;
    }

    public void updateGuestBook(GuestBook guestBook){
        this.guestBook = guestBook;
        guestBook.updateUser(this);
    }

    public void updateUser(UserDto userDto) {
        this.profileUrl = userDto.getProfileUrl();
        this.nickName = userDto.getNickName();
        this.repoUrl = userDto.getRepoUrl();
        this.profileMusicUrl = userDto.getProfileMusicUrl();
        this.profileMusicName = userDto.getProfileMusicName();
        this.fcm = userDto.getFcm();
    }
}
