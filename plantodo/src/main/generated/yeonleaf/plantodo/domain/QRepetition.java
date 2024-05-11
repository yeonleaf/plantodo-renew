package yeonleaf.plantodo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRepetition is a Querydsl query type for Repetition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRepetition extends EntityPathBase<Repetition> {

    private static final long serialVersionUID = 173267969L;

    public static final QRepetition repetition = new QRepetition("repetition");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> repOption = createNumber("repOption", Integer.class);

    public final StringPath repValue = createString("repValue");

    public QRepetition(String variable) {
        super(Repetition.class, forVariable(variable));
    }

    public QRepetition(Path<? extends Repetition> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRepetition(PathMetadata metadata) {
        super(Repetition.class, metadata);
    }

}

