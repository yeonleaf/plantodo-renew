package yeonleaf.plantodo.assembler;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import yeonleaf.plantodo.controller.CheckboxController;
import yeonleaf.plantodo.dto.CheckboxResDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CheckboxModelAssembler implements RepresentationModelAssembler<CheckboxResDto, EntityModel<CheckboxResDto>> {
    @Override
    public CollectionModel<EntityModel<CheckboxResDto>> toCollectionModel(Iterable<? extends CheckboxResDto> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }

    @Override
    public EntityModel<CheckboxResDto> toModel(CheckboxResDto entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(CheckboxController.class).one(entity.getId())).withSelfRel(),
                linkTo(methodOn(CheckboxController.class).delete(entity.getId())).withRel("deletion"),
                linkTo(methodOn(CheckboxController.class).change(entity.getId())).withRel("switch")
                );
    }
}
