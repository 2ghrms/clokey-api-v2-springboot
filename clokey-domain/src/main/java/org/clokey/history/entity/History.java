package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "history",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_history_member_id_history_date",
                    columnNames = {"member_id", "history_date"})
        })
public class History extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private LocalDate historyDate;

    @Column(length = 200)
    private String content;

    @NotNull private boolean banned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id")
    @NotNull
    private Situation situation;

    @OneToMany(mappedBy = "history")
    private List<HistoryImage> historyImages = new ArrayList<>();

    @OneToMany(mappedBy = "history")
    private List<HistoryStyle> historyStyles = new ArrayList<>();

    @OneToMany(mappedBy = "history")
    private List<HistoryHashtag> historyHashtags = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private History(
            LocalDate historyDate,
            String content,
            boolean banned,
            Member member,
            Situation situation) {
        this.historyDate = historyDate;
        this.content = content;
        this.banned = banned;
        this.member = member;
        this.situation = situation;
    }

    public static History createHistory(
            LocalDate historyDate, String content, Member member, Situation situation) {
        return History.builder()
                .historyDate(historyDate)
                .content(content)
                .banned(false)
                .member(member)
                .situation(situation)
                .build();
    }

    public void updateHistory(String content, Situation situation) {
        this.content = content;
        this.situation = situation;
    }
}
