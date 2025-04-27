package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /*
     *   주문
     * */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        //엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        /*
        *   유지보수 시점에서, 만일 팀의 다른 개발자가 OrderItem oi = new OrderItem();을 한다면
        *   로직이 분산되면서 유지보수가 어려워짐
        *   -> 생성자를 protected 레벨로 설정(ex. @NoArgsConstructor(access = AccessLevel.PROTECTED)
        * */

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        orderRepository.save(order);
        /*
        *   원래는 Delivery Repository를 생성하고 .save()를 하고 orderItem도 jpa에 값을 넘겨주고 세팅해야하지만
        *   orderRepository.save(order); 이것만 한 이유:
        *   Order entity에서 OrderItem, Delivery를 cascade ALL 했기 때문.
        *   -> Order를 persist하면 collection에 있는 orderitem도 persist를 강제로 날려줌. delivery도 마찬가지
        *
        *   그렇다면 cascade의 범위를 어디까지?
        *   -> 참조하는 주인이 private owner인 경우에만 사용
        *   : OrderItem은 Order만 참조해서 사용, Delivery도 마찬가지
        *   즉, LifeCycle에 대해서 동일하게 관리를 하고, 다른 것이 참조할 수 없는 private owner인 경우
        *
        *   만약, Delivery가 중요하여 다른 곳에서도 Delivery를 참조하고 사용하면 cascade를 쓰면 안됌
        *   -> 자칫 데이터 정보가 삭제되거나 persist도 복잡하게 얽힐 가능성이 있음
        *   -> 이런 경우에는 별도의 repository를 생성하고 persist 하는 것이 나음
        * */

        return order.getId();
    }

    /*
     * 주문 취소
     * */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
        /*
        *   sql를 직접 다루는 스타일에선 데이터를 끄집어내고
        *   쿼리에 파라미터를 넣는 Transactionl script를 작성해야했음
        *   그러나, JPA에서는 데이터를 바꾸면 JPA가 Dirty Checking을 실행함
        *   즉, 변경 내역 감지를 실행하여 db에 업데이트 쿼리가 날라감
        * */
    }
    //도메인 모델 패턴: 엔티티가 비즈니스 로직을 지니고 객체 지향의 특성을 적용한 스타일(현재 적용한 스타일)
    //트랜잭션 스크립트 패턴: 서비스 계층에서 비즈니스 로직을 처리하고 엔티티에는 비즈니스 로직이 거의 없는 스타일



    //검색
//    public List<Order> findOrders(OrderSearch orderSearch) {
//        return orderRepository.findAll(orderSearch);
//    }


}
