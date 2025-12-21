package kr.muroom.muroombackendbach.common.util.tsid;

import com.github.f4b6a3.tsid.TsidFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.hibernate.annotations.IdGeneratorType;

@IdGeneratorType(TsidGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Tsid {

  Class<? extends Supplier<TsidFactory>> value() default FactorySupplierCustom.class;

  class FactorySupplierCustom implements Supplier<TsidFactory> {

    private final TsidFactory tsidFactory;

    private static final int NODE_BITS = 8; // 최대 256개 노드(서버/컨테이너) 지원
    private static final String CLOCK = "UTC";
    public static final FactorySupplierCustom INSTANCE = new FactorySupplierCustom();

    public FactorySupplierCustom() {
      this.tsidFactory = TsidFactory.builder()
          .withNodeBits(NODE_BITS)
          .withClock(Clock.system(ZoneId.of(CLOCK)))
          .withRandomFunction(() -> ThreadLocalRandom.current().nextInt())
          .build();
    }

    @Override
    public TsidFactory get() {
      return this.tsidFactory;
    }
  }
}