package yeonleaf.plantodo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCheckbox is a Querydsl query type for Checkbox
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCheckbox extends EntityPathBase<Checkbox> {

    private static final long serialVersionUID = 561008467L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCheckbox checkbox = new QCheckbox("checkbox");

    public final BooleanPath checked = createBoolean("checked");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final QGroup group;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath title = createString("title");

    public QCheckbox(String variable) {
        this(Checkbox.class, forVariable(variable), INITS);
    }

    public QCheckbox(Path<? extends Checkbox> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCheckbox(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCheckbox(PathMetadata metadata, PathInits inits) {
        this(Checkbox.class, metadata, inits);
    }

    public QCheckbox(Class<? extends Checkbox> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.group = inits.isInitialized("group") ? new QGroup(forProperty("group"), inits.get("group")) : null;
    }

}

