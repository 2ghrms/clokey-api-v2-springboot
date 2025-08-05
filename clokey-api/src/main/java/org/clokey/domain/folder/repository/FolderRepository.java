package org.clokey.domain.folder.repository;

import org.clokey.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {}
