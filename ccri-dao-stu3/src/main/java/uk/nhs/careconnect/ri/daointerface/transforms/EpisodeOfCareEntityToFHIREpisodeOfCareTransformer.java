package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.BaseAddress;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareIdentifier;

@Component
public class EpisodeOfCareEntityToFHIREpisodeOfCareTransformer implements Transformer<EpisodeOfCareEntity, EpisodeOfCare> {

    private final Transformer<BaseAddress, Address> addressTransformer;

    public EpisodeOfCareEntityToFHIREpisodeOfCareTransformer(@Autowired Transformer<BaseAddress, Address> addressTransformer) {
        this.addressTransformer = addressTransformer;
    }

    @Override
    public EpisodeOfCare transform(final EpisodeOfCareEntity episodeEntity) {
        final EpisodeOfCare episode = new EpisodeOfCare();

        Meta meta = new Meta();
        //.addProfile(CareConnectProfile.EpisodeOfCare_1);

        if (episodeEntity.getUpdated() != null) {
            meta.setLastUpdated(episodeEntity.getUpdated());
        }
        else {
            if (episodeEntity.getCreated() != null) {
                meta.setLastUpdated(episodeEntity.getCreated());
            }
        }
        episode.setMeta(meta);

        episode.setId(episodeEntity.getId().toString());

        for(EpisodeOfCareIdentifier identifier : episodeEntity.getIdentifiers())
        {
            episode.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }
        if (episodeEntity.getPatient() != null) {
            episode.setPatient(new Reference("Patient/"+episodeEntity.getPatient().getId()));
        }
        if (episodeEntity.getManagingOrganisation() != null) {
            episode.setManagingOrganization(new Reference("Organization/"+episodeEntity.getManagingOrganisation().getId()));
        }
        if (episodeEntity.getType() != null) {
            episode.addType().addCoding()
                    .setCode(episodeEntity.getType().getCode())
                    .setSystem(episodeEntity.getType().getSystem())
                    .setDisplay(episodeEntity.getType().getDisplay());
        }

        if (episodeEntity.getPeriodStartDate() != null || episodeEntity.getPeriodEndDate() != null)
        {
            Period period = new Period();
            if (episodeEntity.getPeriodStartDate() != null ) {
               period.setStart(episodeEntity.getPeriodStartDate());
            }
            if (episodeEntity.getPeriodEndDate() != null) {
                period.setEnd(episodeEntity.getPeriodEndDate());
            }
            episode.setPeriod(period);
        }




       

        return episode;

    }
}