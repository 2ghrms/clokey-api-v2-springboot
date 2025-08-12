package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
    @JoinColumn(name = "history_type_id")
    @NotNull
    private HistoryType historyType;

    @Builder(access = AccessLevel.PRIVATE)
    private History(
            LocalDate historyDate,
            String content,
            boolean banned,
            Member member,
            HistoryType historyType) {
        this.historyDate = historyDate;
        this.content = content;
        this.banned = banned;
        this.member = member;
        this.historyType = historyType;
    }

    public static History creatHistory(
            LocalDate historyDate, String content, Member member, HistoryType historyType) {
        return History.builder()
                .historyDate(historyDate)
                .content(content)
                .banned(false)
                .member(member)
                .historyType(historyType)
                .build();
    }
}
