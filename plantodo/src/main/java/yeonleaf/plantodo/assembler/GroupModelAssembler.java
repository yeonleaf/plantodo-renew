package yeonleaf.plantodo.assembler;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import yeonleaf.plantodo.controller.CheckboxController;
import yeonleaf.plantodo.controller.GroupController;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.PlanResDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GroupModelAssembler implements RepresentationModelAssembler<GroupResDto, EntityModel<GroupResDto>> {
    @Override
    public CollectionModel<EntityModel<GroupResDto>> toCollectionModel(Iterable<? extends GroupResDto> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }

    @Override
    public EntityModel<GroupResDto> toModel(GroupResDto entity) {

        return EntityModel.of(entity,
                linkTo(methodOn(GroupController.class).one(entity.getId())).withSelfRel(),
                linkTo(methodOn(CheckboxController.class).all("group", entity.getId())).withRel("lower-collection"),
                linkTo(methodOn(GroupController.class).delete(entity.getId())).withRel("deletion"));

    }
}
