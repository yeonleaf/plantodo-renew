package yeonleaf.plantodo.assembler;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import yeonleaf.plantodo.controller.CheckboxController;
import yeonleaf.plantodo.controller.GroupController;
import yeonleaf.plantodo.controller.PlanController;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanResDto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PlanModelAssembler implements RepresentationModelAssembler<PlanResDto, EntityModel<PlanResDto>> {
        @Override
    public EntityModel<PlanResDto> toModel(PlanResDto planResDto) {
        return EntityModel.of(planResDto,
                linkTo(methodOn(PlanController.class).one(planResDto.getId())).withSelfRel(),
                linkTo(methodOn(GroupController.class).all(planResDto.getId())).withRel("lower-collection"),
                linkTo(methodOn(CheckboxController.class).all("plan", planResDto.getId())).withRel("lower-collection"),
                linkTo(methodOn(PlanController.class).delete(planResDto.getId())).withRel("deletion"),
                linkTo(methodOn(PlanController.class).change(planResDto.getId())).withRel("changing")
        );
    }
}
