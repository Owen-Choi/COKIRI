package f3f.dev1.domain.post.dao;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import f3f.dev1.domain.member.dto.MemberDTO;
import f3f.dev1.domain.message.model.QMessageRoom;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.post.dto.PostDTO;
import f3f.dev1.domain.post.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.util.StringUtils.isNullOrEmpty;
import static f3f.dev1.domain.message.model.QMessageRoom.*;
import static f3f.dev1.domain.post.dto.PostDTO.*;
import static f3f.dev1.domain.post.dto.PostDTO.SearchPostRequestExcludeTag;
import static f3f.dev1.domain.post.model.QPost.post;
import static f3f.dev1.domain.post.model.QScrapPost.scrapPost;
import static f3f.dev1.domain.scrap.model.QScrap.scrap;
import static f3f.dev1.domain.tag.model.QPostTag.postTag;
import static f3f.dev1.domain.tag.model.QTag.tag;
import static f3f.dev1.domain.trade.model.QTrade.trade;

@Slf4j
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> findPostDTOByConditions(SearchPostRequestExcludeTag requestExcludeTag, Pageable pageable) {
        List<Post> results = findPostListWithConditionWithoutTag(requestExcludeTag, pageable);
        JPAQuery<Long> countQuery = getCount(requestExcludeTag);
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // 쿼리 DSL에서는 Enum 타입만 조회할 수 있고 세부 필드는 조회할 수 없다.
    private List<Post> findPostListWithConditionWithoutTag(SearchPostRequestExcludeTag requestExcludeTag, Pageable pageable) {
        List<Post> results = jpaQueryFactory.
                selectFrom(post)
                .where(productCategoryNameFilter(requestExcludeTag.getProductCategory()),
                        wishCategoryNameFilter(requestExcludeTag.getWishCategory()),
                        priceFilter(requestExcludeTag.getMinPrice(), requestExcludeTag.getMaxPrice()))
                // TradeStatus를 비교하는 아래 조건은 필수 조건이기 때문에 BooleanExpression을 활용한 동적 쿼리로 짜지는 않겠다.
                .where(post.trade.tradeStatus.eq(requestExcludeTag.getTradeStatus()))
                .orderBy(dynamicSorting(pageable.getSort()).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return results;
    }

    private JPAQuery<Long> getCount(SearchPostRequestExcludeTag requestExcludeTag) {
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(productCategoryNameFilter(requestExcludeTag.getProductCategory()),
                        wishCategoryNameFilter(requestExcludeTag.getWishCategory()),
                        priceFilter(requestExcludeTag.getMinPrice(), requestExcludeTag.getMaxPrice()))
                .where(post.trade.tradeStatus.eq(requestExcludeTag.getTradeStatus()));
        return countQuery;
    }


    @Override
    public Page<PostSearchResponseDto> findPostsByTags(List<String>tagNames, TradeStatus tradeStatus, Pageable pageable) {
        List<PostSearchResponseDto> result = findPostsByTagsQuery(tagNames, tradeStatus, pageable);
        // 카운트 쿼리
        // 최적화할 수 있다. 카운트 쿼리에서는 스크랩 여부는 알 필요가 없으니 조인을 2번이나 줄일 수 있다.
        JPAQuery<Long> countQuery = jpaQueryFactory
            .select(post.count())
            .from(post)
            .join(post.trade, trade)
            .join(post.postTags, postTag)
            .join(postTag.tag, tag)
            .where(tag.name.in(tagNames))
            .where(trade.tradeStatus.eq(tradeStatus))
            .groupBy(post.id);
        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    private List<PostSearchResponseDto> findPostsByTagsQuery(List<String> tagNames, TradeStatus tradeStatus, Pageable pageable) {
        return jpaQueryFactory
            .select(Projections.constructor(PostSearchResponseDto.class,
                post.id,
                post.title,
                post.content,
                post.thumbnailImgPath,
                post.author.nickname,
                post.productCategory.name,
                post.createDate,
                messageRoom.id.count(),
                post.wishCategory.name,
                scrapPost.post.id.when(post.id)
                    .then(true)
                    .otherwise(false),
                scrapPost.post.id.count(),
                post.price
                ))
            .from(post)
            .join(post.trade, trade)
            .join(post.postTags, postTag)
            .leftJoin(scrapPost).on(scrapPost.post.id.eq(post.id))
            .leftJoin(messageRoom).on(messageRoom.post.id.eq(post.id))
            .join(postTag.tag, tag)
            .where(tag.name.in(tagNames))
            .where(trade.tradeStatus.eq(tradeStatus))
            .groupBy(post.id)
            .orderBy(dynamicSorting(pageable.getSort()).toArray(OrderSpecifier[]::new))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public Page<Post> findPostsWithTradeStatus(TradeStatus tradeStatus, Pageable pageable) {
        List<Post> results = findPostsWithTradeStatusQuery(tradeStatus, pageable);
        // 카운트 쿼리
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(post.trade.tradeStatus.eq(tradeStatus));
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    private List<Post> findPostsWithTradeStatusQuery(TradeStatus tradeStatus, Pageable pageable) {
        List<Post> results = jpaQueryFactory
                .selectFrom(post)
                .where(post.trade.tradeStatus.eq(tradeStatus))
                .orderBy(dynamicSorting(pageable.getSort()).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return results;
    }

    @Override
    public Page<MemberDTO.GetUserPost> getUserPostById(Long memberId, Pageable pageable){
        List<MemberDTO.GetUserPost> userPostList = getUserPostList(memberId);
        JPAQuery<Long> countQuery = jpaQueryFactory
            .select(post.id.count())
            .from(post)
            .where(post.author.id.eq(memberId));
        return PageableExecutionUtils.getPage(userPostList, pageable, countQuery::fetchOne);
    }

    private List<MemberDTO.GetUserPost> getUserPostList(Long memberId) {
        return jpaQueryFactory.select(Projections.constructor(
                MemberDTO.GetUserPost.class,
                post.id,
                post.thumbnailImgPath,
                post.title,
                post.trade.tradeStatus,
                post.wishCategory.name,
                scrapPost.id.count()
            ))
            .from(post)
            .leftJoin(scrapPost).on(post.id.eq(scrapPost.post.id))
            .where(post.author.id.eq(memberId))
            .groupBy(post.id, post.trade.id)
            .fetch();
    }

    private BooleanExpression productCategoryNameFilter(String productCategoryName) {
        return StringUtils.hasText(productCategoryName) ? post.productCategory.name.eq(productCategoryName) : null;
    }

    private BooleanExpression wishCategoryNameFilter(String wishCategoryName) {
        return StringUtils.hasText(wishCategoryName) ? post.wishCategory.name.eq(wishCategoryName) : null;
    }

    private BooleanExpression priceFilter(String minPrice, String maxPrice) {
        if(isNullOrEmpty(minPrice) && isNullOrEmpty(maxPrice)) {
            return null;
        }
        // 둘중 하나만 값이 있는 경우를 처리해주겠음
        if(isNullOrEmpty(minPrice) && !isNullOrEmpty(maxPrice)) {
            return post.price.loe(Long.parseLong(maxPrice));
        } else if(!isNullOrEmpty(minPrice) && isNullOrEmpty(maxPrice)) {
            return post.price.goe(Long.parseLong(minPrice));
        }
        // 어느 조건문에도 걸리지 않으면 둘 다 존재하는 경우. between으로 처리함.
        return post.price.between(Long.parseLong(minPrice), Long.parseLong(maxPrice));
    }

    private BooleanExpression memberFilter(Long currentMemberId) {
        if(currentMemberId == null) {
            return null;
        }
        return scrap.member.id.eq(currentMemberId);
    }

    private List<OrderSpecifier> dynamicSorting(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        sort.stream().forEach(order -> {
            Order direction= order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            // 대상이 되는 클래스를 지정하고, 그 클래스 내에서 세부
            PathBuilder orderByExpression = new PathBuilder(Post.class, "post");
            orders.add(new OrderSpecifier(direction, orderByExpression.get(prop)));
        });
        return orders;
    }
}
