package ru.gkpromtech.exhibition.schema;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qozix.tileview.TileView;
import com.qozix.tileview.hotspots.HotSpot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.gkpromtech.exhibition.ExhibitionApplication;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.MapsPoint;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.organizations.OrganizationDetailActivity;
import ru.gkpromtech.exhibition.organizations.OrganizationItem;

public class SchemaFragment extends Fragment {

    public interface SchemaFragmentCallbacks {
        void onPlaceClicked(String placeId);
    }

    private final static String ARG_MARKERS = "markers";
    private final static String ARG_SCHEMA_EXT = "schema_ext";
    private final static String ARG_SCHEMA_WIDTH = "schema_width";
    private final static String ARG_SCHEMA_HEIGHT = "schema_height";
    private final static String ARG_SCHEMA_SCALES = "schema_scales";

    private TileView mTileView;
    private SchemaFragmentCallbacks mSchemaFragmentCallbacks;
    private Set<Integer> mVisibleMarkerTypes = new HashSet<>();

    public SchemaFragment() {
        // Required empty public constructor
    }

    public static SchemaFragment newInstance(Context context, List<SchemaActivity.Marker> markers) {
        String line = null;

        try {
            InputStream inputStream = context.getAssets().open("schema/info");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            line = bufferedReader.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (line == null) {
            Log.e("SchemaFragment", "Cannot get schema info");
            return null;
        }

        String[] data = line.split(" ");

        if (data.length < 4) {
            Log.e("SchemaFragment", "Cannot parse schema info");
            return null;
        }

        String ext = data[0];
        int width = Integer.parseInt(data[1]);
        int height = Integer.parseInt(data[2]);
        int scalesLen = data.length - 3;
        int[] scales = new int[scalesLen];

        for (int i = 0; i < scalesLen; ++i) {
            scales[i] = Integer.parseInt(data[3 + i]);
        }

        SchemaFragment fragment = new SchemaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCHEMA_EXT, ext);
        args.putInt(ARG_SCHEMA_WIDTH, width);
        args.putInt(ARG_SCHEMA_HEIGHT, height);
        args.putIntArray(ARG_SCHEMA_SCALES, scales);

        if (markers != null) {
            args.putSerializable(ARG_MARKERS, (Serializable) markers);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSchemaFragmentCallbacks = (SchemaFragmentCallbacks) getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTileView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTileView.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTileView.destroy();
        mTileView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View root = inflater.inflate(R.layout.fragment_schema, container, false);
        mTileView = (TileView) root.findViewById(R.id.tileView);

        root.findViewById(R.id.buttonZoomIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTileView.setScaleFromCenter(mTileView.getScale() + 0.25f);
            }
        });

        root.findViewById(R.id.buttonZoomOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTileView.setScaleFromCenter(mTileView.getScale() - 0.25f);
            }
        });

        root.findViewById(R.id.buttonMarkers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMarkerTypes();
            }
        });

        initSchema();

        return root;
    }

    private void chooseMarkerTypes() {
        String[] markersNames = getResources().getStringArray(R.array.marker_types);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.show)
                .setItems(markersNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        showMarkerTypes(index);
                    }
                })
                .show();
    }

    private void showMarkerTypes(int index) {
        String[] markersNames = getResources().getStringArray(R.array.marker_types);
        String[] markerPrefixes = getResources().getStringArray(R.array.marker_types_prefix);

        if (mVisibleMarkerTypes.contains(index)) {
            return;
        }

        mVisibleMarkerTypes.add(index);
        Context context = getActivity();

        int markerResId = getResources().getIdentifier("ic_marker_" + markerPrefixes[index],
                "drawable", ExhibitionApplication.class.getPackage().getName());

        if (markerResId == 0)  {
            return;
        }

        View view = getActivity().getWindow().getDecorView();
        int size = Math.min(view.getWidth(), view.getHeight()) / 10;

        Table<MapsPoint> mapsPointTable = DbHelper.getInstance(context).getTableFor(MapsPoint.class);
        List<MapsPoint> mapsPoints = mapsPointTable.select("mapid is null and placename LIKE ?",
                new String[] { markerPrefixes[index] + "%" }, null, null, null);

        for (MapsPoint point : mapsPoints) {
            List<double[]> points = parsePoints(point.coords);

            if (points.size() == 0) {
                continue;
            }

            PointF center = getCenter(points);
            ImageView imageView = new ImageView(context);
            SchemaActivity.Marker marker = new SchemaActivity.Marker(
                    point.placename, markersNames[index]);
            imageView.setTag(new Pair<>(marker, center));
            imageView.setImageResource(markerResId);
            imageView.setOnClickListener(mOnSpecialMarkerClickListener);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mTileView.addMarker(imageView, center.x, center.y, null, null);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = size;
            layoutParams.height = size;
            imageView.setLayoutParams(layoutParams);
        }
    }

    private void initSchema() {
        Bundle args = getArguments();
        String ext = args.getString(ARG_SCHEMA_EXT);
        int width = args.getInt(ARG_SCHEMA_WIDTH);
        int height = args.getInt(ARG_SCHEMA_HEIGHT);
        int[] scales = args.getIntArray(ARG_SCHEMA_SCALES);
        @SuppressWarnings("unchecked")
        List<SchemaActivity.Marker> markers = (List<SchemaActivity.Marker>) args.getSerializable(ARG_MARKERS);

        mTileView.setSize(width, height);
        mTileView.setMarkerAnchorPoints(-0.5f, -1.0f);
        mTileView.setScaleLimits(0, 2);
        mTileView.setShouldScaleToFit(true);
        mTileView.setShouldRenderWhilePanning(true);

        for (int scale : scales) {
            float detailScale = scale / 1000f;
            String detailData = "schema/" + scale + "/%d_%d." + ext;
            mTileView.addDetailLevel(detailScale, detailData, 256, 256);
        }

        Bitmap downsample = null;

        try {
            InputStream inputStream = getActivity().getAssets().open("schema/preview.png");
            downsample = BitmapFactory.decodeStream(inputStream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (downsample != null) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageBitmap(downsample);
            mTileView.addView(imageView, 0);
        }

        mTileView.setScale(0.5f);

        PointF centerTo = null;
        ImageView imageViewCenterTo = null;

        DbHelper db = DbHelper.getInstance(getActivity());
        Table<MapsPoint> pointsTable = db.getTableFor(MapsPoint.class);

        String[] excludeMarkerPrefixes = getResources().getStringArray(R.array.marker_types_prefix);
        List<MapsPoint> mapsPoints = pointsTable.select("mapid is null", null, null, null, "placename");

        HotSpot.HotSpotTapListener hotSpotTapListener = new HotSpot.HotSpotTapListener() {
            @Override
            public void onHotSpotTap(HotSpot hotSpot, int x, int y) {
                String placeName = (String) hotSpot.getTag();
                mSchemaFragmentCallbacks.onPlaceClicked(placeName);
            }
        };

        for (MapsPoint point : mapsPoints) {
            boolean invisible = false;

            for (String excludeMarker : excludeMarkerPrefixes) {
                if (point.placename.startsWith(excludeMarker)) {
                    invisible = true;
                    break;
                }
            }

            if (!invisible) {
                List<double[]> points = parsePoints(point.coords);

                if (!points.isEmpty()) {
                    HotSpot hotSpot = mTileView.addHotSpot(points, hotSpotTapListener);
                    hotSpot.setTag(point.placename);
                }
            }
        }

        if (markers != null) {
            for (SchemaActivity.Marker marker : markers) {
                int pointIndex = -1;

                for (int i = 0; i < mapsPoints.size(); i++) {
                    if (marker.placeId.equals(mapsPoints.get(i).placename)) {
                        pointIndex = i;
                        break;
                    }
                }

                if (pointIndex == -1) {
                    continue;
                }

                List<double[]> points = parsePoints(mapsPoints.get(pointIndex).coords);

                if (points.size() == 0) {
                    continue;
                }

                PointF center = getCenter(points);
                ImageView imageView = new ImageView(getActivity());
                imageView.setTag(new Pair<>(marker, center));
                imageView.setImageResource(R.drawable.ic_marker);
                imageView.setOnClickListener(mOnMarkerClickListener);
                mTileView.addMarker(imageView, center.x, center.y, null, null);

                if (centerTo == null) {
                    centerTo = center;
                    imageViewCenterTo = imageView;
                }
            }
        }

        // frame to first marker or center
        final PointF finalCenterTo = (centerTo != null) ? centerTo
                : new PointF(width / 2, height / 2);
        final ImageView finalImageViewCenterTo = imageViewCenterTo;

        mTileView.post(new Runnable() {
            @Override
            public void run() {
                mTileView.scrollToAndCenter(finalCenterTo.x, finalCenterTo.y);

                if (finalImageViewCenterTo != null) {
                    mOnMarkerClickListener.onClick(finalImageViewCenterTo);
                }
            }
        });
    }

    private List<double[]> parsePoints(String coordinates) {
        String[] xy = coordinates.split(",");
        List<double[]> points = new ArrayList<>(xy.length / 2);

        for (int i = 0; i < xy.length; i += 2) {
            points.add(new double[]{ Double.parseDouble(xy[i]), Double.parseDouble(xy[i + 1]) });
        }

        return points;
    }

    private PointF getCenter(List<double[]> points) {
        float length = points.size();
        float x = 0;
        float y = 0;

        for (double[] point : points) {
            x += point[0] / length;
            y += point[1] / length;
        }

        return new PointF(x, y);
    }

    private View.OnClickListener mOnSpecialMarkerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            @SuppressWarnings("unchecked")
            Pair<SchemaActivity.Marker, PointF> markerPos = (Pair<SchemaActivity.Marker, PointF>) view.getTag();

            mSchemaFragmentCallbacks.onPlaceClicked(markerPos.first.placeId);
        }
    };

    private View.OnClickListener mOnMarkerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            @SuppressWarnings("unchecked")
            Pair<SchemaActivity.Marker, PointF> markerPos =
                    (Pair<SchemaActivity.Marker, PointF>) view.getTag();

            final SchemaActivity.Marker marker = markerPos.first;
            PointF center = markerPos.second;

            mTileView.slideToAndCenter(center.x, center.y);

            SchemaCallout callout = new SchemaCallout(view.getContext());
            callout.setTitle(marker.text);

            if (marker.organization != null) {
                callout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        OrganizationItem organizationItem = new OrganizationItem(new Place(),
                                marker.organization);
                        Intent intent = new Intent(getActivity(), OrganizationDetailActivity.class);
                        intent.putExtra("organization", organizationItem);
                        startActivity(intent);
                    }
                });
            }

            mTileView.addCallout(callout, center.x, center.y, -0.5f, -1.0f);
            callout.transitionIn();
        }
    };
}
