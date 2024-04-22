package f3f.dev1.domain.scrap.dao;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import f3f.dev1.domain.post.model.QScrapPost;
import f3f.dev1.domain.post.model.ScrapPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static f3f.dev1.domain.category.model.QCategory.category;
import static f3f.dev1.domain.post.model.QPost.post;
import static f3f.dev1.domain.post.model.QScrapPost.scrapPost;
import static f3f.dev1.domain.scrap.dto.ScrapPostDTO.GetUserScrapPost;
import static f3f.dev1.domain.trade.model.QTrade.trade;

@RequiredArgsConstructor
@Slf4j
public class ScrapPostRepositoryImpl implements ScrapPostCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<GetUserScrapPost> findUserScrapPost(Long scrapId, Pageable pageable) {
    List<GetUserScrapPost> userScrapPostList = getUserScrapPostList(scrapId);
    QScrapPost newScrapPost = new QScrapPost("newScrapPost");
    JPAQuery<Long> countQuery = queryFactory
        .select(scrapPost.id.count())
        .from(scrapPost)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .where(scrapPost.post.id.in(
            JPAExpressions
                .select(newScrapPost.post.id)
                .from(newScrapPost)
                .where(newScrapPost.scrap.id.eq(scrapId))
        ));
    return PageableExecutionUtils.getPage(userScrapPostList, pageable, countQuery::fetchOne);
  }

  private List<GetUserScrapPost> getUserScrapPostList(Long scrapId) {
    QScrapPost newScrapPost = new QScrapPost("newScrapPost");
    return queryFactory
        .select(
            Projections.constructor(
                GetUserScrapPost.class,
                post.id,
                post.title,
                post.thumbnailImgPath,
                post.trade.tradeStatus,
                category.name,
                scrapPost.scrap.count()
            )
        ).from(scrapPost)
        .join(scrapPost.post, post)
        .join(post.wishCategory, category)
        .where(scrapPost.post.id.in(
            JPAExpressions
                .select(newScrapPost.post.id)
                .from(newScrapPost)
                .where(newScrapPost.scrap.id.eq(scrapId))
        )).groupBy(scrapPost.post.id, post.trade.id)
        .fetch();
  }
}
