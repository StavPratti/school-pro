package gr.aueb.cf.schoolapp.rest;

import gr.aueb.cf.schoolapp.core.exceptions.*;
import gr.aueb.cf.schoolapp.dto.*;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.service.ITeacherService;
import gr.aueb.cf.schoolapp.validator.ValidatorUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Path("/teachers")
public class TeacherRestController {

    private final ITeacherService teacherService;

//    Constructor with final ITeacherService
//    @Inject
//    public TeacherRestController(ITeacherService teacherService) {
//        this.teacherService = teacherService;
//    }


    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTeacher(TeacherInsertDTO insertDTO, @Context UriInfo uriInfo)
            throws EntityInvalidArgumentException, EntityAlreadyExistsException {
        List<String> errors = ValidatorUtil.validateDTO(insertDTO);
        if (!errors.isEmpty()) {
            throw new EntityInvalidArgumentException("Teacher", String.join("\n", errors));
        }

        // Here the new URI that is created, with the new id is built
        TeacherReadOnlyDTO readOnlyDTO = teacherService.insertTeacher(insertDTO);
        URI newResourceUri = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(readOnlyDTO.getId()))
                .build();

        return Response
                .created(newResourceUri)
                .entity(readOnlyDTO).build();
    }

    @PUT
    @Path("/{teacherId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTeacher(@PathParam("teacherId") Long teacherId, TeacherUpdateDTO updateDTO)
            throws EntityInvalidArgumentException, EntityNotFoundException {
        List<String> errors = ValidatorUtil.validateDTO(updateDTO);
        if (!errors.isEmpty()) {
            throw new EntityInvalidArgumentException("Teacher", String.join("\n", errors));
        }

        TeacherReadOnlyDTO readOnlyDTO = teacherService.updateTeacher(updateDTO);
        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO).build();
    }

    @DELETE
    @Path("/{teacherId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTeacher(@PathParam("teacherId") Long teacherId)
            throws EntityNotFoundException {

        TeacherReadOnlyDTO readOnlyDTO = teacherService.getTeacherById(teacherId);
        teacherService.deleteTeacher(teacherId);
        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }

    @GET
    @Path("/{teacherId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeacher(@PathParam("teacherId") Long id)
            throws EntityNotFoundException {
        TeacherReadOnlyDTO readOnlyDTO = teacherService.getTeacherById(id);
        return Response
                .status(Response.Status.OK)
                .entity(readOnlyDTO)
                .build();
    }


    @GET
    @Path("/filtered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFiltered(@QueryParam("firstname") @DefaultValue("") String firstname,
                                @QueryParam("lastname") @DefaultValue("") String lastname,
                                @QueryParam("vat") @DefaultValue("") String vat) {
        TeacherFiltersDTO filtersDTO = new TeacherFiltersDTO(firstname, lastname, vat);
        Map<String , Object> criteria;

        criteria = Mapper.mapToCriteria(filtersDTO);
        List<TeacherReadOnlyDTO> readOnlyDTOS = teacherService.getTeachersByCriteria(criteria);
        return Response.status(Response.Status.OK)
                .entity(readOnlyDTOS)
                .build();
    }


    @GET
    @Path("/paginated")
    @Produces(MediaType.APPLICATION_JSON)
    public PaginatedResult<TeacherReadOnlyDTO> getFilteredPaginated(
            @QueryParam("firstname") @DefaultValue("") String firstname,
            @QueryParam("lastname") @DefaultValue("") String lastname,
            @QueryParam("vat") @DefaultValue("") String vat,
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size
    )
            throws EntityInvalidArgumentException {
        TeacherFiltersDTO filtersDTO = new TeacherFiltersDTO(firstname, lastname, vat);
        Map<String , Object> criteria;

        criteria = Mapper.mapToCriteria(filtersDTO);

        if (page < 0) throw new EntityInvalidArgumentException("PageInvalidNumber", "Invalid page number");
        if (size <= 0) throw new EntityInvalidArgumentException("SizeInvalidNumber", "Invalid size number");

        List<TeacherReadOnlyDTO> readOnlyDTOS = teacherService.getTeachersByCriteriaPaginated(criteria, page, size);
        long totalItems = teacherService.getTeachersCountByCriteria(criteria);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        return new PaginatedResult<>(
                readOnlyDTOS,
                page,
                size,
                totalPages,
                totalItems
        );
    }
}
