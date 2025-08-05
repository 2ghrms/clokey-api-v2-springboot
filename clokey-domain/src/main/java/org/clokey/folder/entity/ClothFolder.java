package org.clokey.folder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.cloth.entity.Cloth;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothFolder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id")
    @NotNull
    private Cloth cloth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    @NotNull
    private Folder folder;

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private ClothFolder(Cloth cloth, Folder folder) {
    //        this.cloth = cloth;
    //        this.folder = folder;
    //    }
    //
    //    public static ClothFolder createClothFolder(Cloth cloth, Folder folder) {
    //        return ClothFolder.builder().cloth(cloth).folder(folder).build();
    //    }
}
