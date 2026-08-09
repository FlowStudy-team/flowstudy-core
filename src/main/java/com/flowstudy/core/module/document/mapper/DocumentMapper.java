package com.flowstudy.core.module.document.mapper;

import com.flowstudy.core.module.document.entity.Document;
import com.flowstudy.core.module.document.entity.DocumentCategory;
import com.flowstudy.core.module.document.entity.DocumentFolder;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DocumentMapper {

    // ---- Document queries ----

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM fs_document d
            WHERE d.deleted = 0
              AND d.user_id = #{userId}
              <if test='keyword != null'>
                AND (d.title LIKE CONCAT('%', #{keyword}, '%')
                     OR d.summary LIKE CONCAT('%', #{keyword}, '%')
                     OR d.content LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test='folderId != null'>
                AND d.folder_id = #{folderId}
              </if>
              <if test='categoryId != null'>
                AND d.category_id = #{categoryId}
              </if>
              <if test='tag != null'>
                AND FIND_IN_SET(#{tag}, d.tags) &gt; 0
              </if>
              <if test='status != null'>
                AND d.status = #{status}
              </if>
            </script>
            """)
    long countByUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("folderId") Long folderId,
            @Param("categoryId") Long categoryId,
            @Param("tag") String tag,
            @Param("status") String status);

    @Select("""
            <script>
            SELECT
                d.id,
                d.user_id,
                d.title,
                d.content,
                d.summary,
                d.folder_id,
                COALESCE(f.name, '') AS folder_name,
                d.category_id,
                COALESCE(c.name, '') AS category_name,
                d.tags,
                d.status,
                d.published_at,
                d.created_at,
                d.updated_at
            FROM fs_document d
            LEFT JOIN fs_document_folder f ON f.id = d.folder_id AND f.deleted = 0
            LEFT JOIN fs_document_category c ON c.id = d.category_id AND c.deleted = 0
            WHERE d.deleted = 0
              AND d.user_id = #{userId}
              <if test='keyword != null'>
                AND (d.title LIKE CONCAT('%', #{keyword}, '%')
                     OR d.summary LIKE CONCAT('%', #{keyword}, '%')
                     OR d.content LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test='folderId != null'>
                AND d.folder_id = #{folderId}
              </if>
              <if test='categoryId != null'>
                AND d.category_id = #{categoryId}
              </if>
              <if test='tag != null'>
                AND FIND_IN_SET(#{tag}, d.tags) &gt; 0
              </if>
              <if test='status != null'>
                AND d.status = #{status}
              </if>
            ORDER BY d.updated_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Document> findPageByUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("folderId") Long folderId,
            @Param("categoryId") Long categoryId,
            @Param("tag") String tag,
            @Param("status") String status,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Select("""
            SELECT
                d.id,
                d.user_id,
                d.title,
                d.content,
                d.summary,
                d.folder_id,
                COALESCE(f.name, '') AS folder_name,
                d.category_id,
                COALESCE(c.name, '') AS category_name,
                d.tags,
                d.status,
                d.published_at,
                d.created_at,
                d.updated_at
            FROM fs_document d
            LEFT JOIN fs_document_folder f ON f.id = d.folder_id AND f.deleted = 0
            LEFT JOIN fs_document_category c ON c.id = d.category_id AND c.deleted = 0
            WHERE d.deleted = 0 AND d.id = #{id}
            LIMIT 1
            """)
    Document findById(@Param("id") Long id);

    @Select("""
            SELECT
                d.id,
                d.user_id,
                d.title,
                d.content,
                d.summary,
                d.folder_id,
                COALESCE(f.name, '') AS folder_name,
                d.category_id,
                COALESCE(c.name, '') AS category_name,
                d.tags,
                d.status,
                d.published_at,
                d.created_at,
                d.updated_at
            FROM fs_document d
            LEFT JOIN fs_document_folder f ON f.id = d.folder_id AND f.deleted = 0
            LEFT JOIN fs_document_category c ON c.id = d.category_id AND c.deleted = 0
            WHERE d.deleted = 0
              AND d.id = #{id}
              AND d.user_id = #{userId}
            LIMIT 1
            """)
    Document findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO fs_document (user_id, title, content, summary, folder_id, category_id, tags, status, published_at)
            VALUES (#{userId}, #{title}, #{content}, #{summary}, #{folderId}, #{categoryId}, #{tags},
                    COALESCE(#{status}, 'draft'),
                    CASE WHEN #{status} = 'published' THEN NOW() ELSE NULL END)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Document doc);

    @Update("""
            UPDATE fs_document
            SET title = #{title},
                content = #{content},
                summary = #{summary},
                folder_id = #{folderId},
                category_id = #{categoryId},
                tags = #{tags},
                status = COALESCE(#{status}, status),
                published_at = CASE WHEN #{status} = 'published' AND published_at IS NULL
                                    THEN NOW() ELSE published_at END,
                updated_at = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int update(Document doc);

    @Update("""
            UPDATE fs_document SET deleted = 1, updated_at = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    // ---- Category queries ----

    @Select("""
            SELECT id, name
            FROM fs_document_category
            WHERE deleted = 0
            ORDER BY id ASC
            """)
    List<DocumentCategory> findAllCategories();

    // ---- Folder queries ----

    @Select("""
            SELECT id, user_id, name, parent_id, created_at, updated_at
            FROM fs_document_folder
            WHERE deleted = 0 AND user_id = #{userId}
            ORDER BY name ASC
            """)
    List<DocumentFolder> findFoldersByUser(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO fs_document_folder (user_id, name, parent_id)
            VALUES (#{userId}, #{name}, #{parentId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFolder(DocumentFolder folder);

    @Update("""
            UPDATE fs_document_folder SET deleted = 1, updated_at = NOW()
            WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
            """)
    int softDeleteFolder(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            SELECT id, user_id, name, parent_id, created_at, updated_at
            FROM fs_document_folder
            WHERE deleted = 0 AND id = #{id}
            LIMIT 1
            """)
    DocumentFolder findFolderById(@Param("id") Long id);
}
