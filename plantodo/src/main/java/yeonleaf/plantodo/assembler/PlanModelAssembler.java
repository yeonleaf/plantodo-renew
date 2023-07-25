package yeonleaf.plantodo.assembler;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
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
                linkTo(methodOn(PlanController.class).delete(planResDto.getId())).withRel("deletion")
//                linkTo(methodOn(PlanController.class).status(planResDto.getId())).withRel("switch")
        );
    }
}
